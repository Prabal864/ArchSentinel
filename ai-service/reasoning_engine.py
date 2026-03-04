import os
import httpx
from pathlib import Path
from typing import Any, Dict, List

PROMPT_TEMPLATE_PATH = Path(__file__).parent / "prompt_templates" / "architecture_review.txt"
OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")
LLM_MODEL = os.getenv("LLM_MODEL", "openai/gpt-4o-mini")


def load_system_prompt() -> str:
    try:
        return PROMPT_TEMPLATE_PATH.read_text(encoding="utf-8")
    except Exception:
        return "You are an expert software architect. Analyze the provided architecture metrics and give recommendations."


def build_user_prompt(impact_report: Dict[str, Any], graph_data: Dict[str, Any], violations: List[str]) -> str:
    lines = ["## Architecture Impact Report\n"]

    risk_level = impact_report.get("riskLevel", impact_report.get("risk_level", "UNKNOWN"))
    health_change = impact_report.get("healthScoreChange", impact_report.get("health_score_change", 0))
    pr_health = impact_report.get("prHealthScore", impact_report.get("pr_health_score", 0))
    base_health = impact_report.get("baseHealthScore", 0)
    total_classes = impact_report.get("totalClasses", 0)
    total_methods = impact_report.get("totalMethods", 0)
    base_classes = impact_report.get("baseTotalClasses", 0)
    base_methods = impact_report.get("baseTotalMethods", 0)

    lines.append(f"**Risk Level:** {risk_level}")
    lines.append(f"**Health Score:** {base_health:.1f} → {pr_health:.1f} (Δ {health_change:+.1f})")
    lines.append(f"**Codebase:** {total_classes} classes, {total_methods} methods (was {base_classes} classes, {base_methods} methods)")

    coupling_delta = impact_report.get("couplingDelta", impact_report.get("coupling_delta", 0))
    lines.append(f"**Coupling Delta:** {coupling_delta:+.1f}")

    complexity_delta = impact_report.get("complexityDelta", impact_report.get("complexity_delta", 0))
    lines.append(f"**Complexity Delta:** {complexity_delta:+.1f}")

    # --- Circular Dependencies ---
    circular_paths = impact_report.get("circularPaths", impact_report.get("circular_paths", []))
    new_circular = impact_report.get("newCircularDependency", impact_report.get("new_circular_dependency", False))
    new_circular_count = impact_report.get("newCircularCount", impact_report.get("new_circular_count", 0))
    if circular_paths:
        lines.append(f"\n### 🔄 Circular Dependencies ({len(circular_paths)} cycles found, {new_circular_count} new):")
        for path in circular_paths:
            lines.append(f"  - `{path}`")
    elif new_circular:
        lines.append(f"**New Circular Dependencies:** {new_circular_count}")

    # --- Layer Violations (pinpointed) ---
    all_violations = impact_report.get("violations", [])
    new_layer = impact_report.get("newLayerViolations", impact_report.get("new_layer_violations", 0))
    if all_violations:
        lines.append(f"\n### 🔴 Layer Violations ({len(all_violations)} total, {new_layer} new):")
        for v in all_violations:
            lines.append(f"  - `{v}`")

    # --- Performance Risks (pinpointed) ---
    all_risks = impact_report.get("risks", [])
    new_perf = impact_report.get("newPerformanceRisks", impact_report.get("new_performance_risks", 0))
    if all_risks:
        lines.append(f"\n### ⚡ Performance Risks ({len(all_risks)} total, {new_perf} new):")
        for r in all_risks:
            lines.append(f"  - `{r}`")

    # --- Coupling Hotspots (pinpointed per-class) ---
    hotspots = impact_report.get("couplingHotspots", [])
    if hotspots:
        lines.append(f"\n### 🔗 Coupling Hotspots (Top {len(hotspots)} most coupled classes):")
        lines.append("| Class | Type | Fan-In | Fan-Out | Total | Depends On | Depended By |")
        lines.append("|-------|------|--------|---------|-------|------------|-------------|")
        for h in hotspots:
            cls_name = h.get("className", "?")
            cls_type = h.get("type", "?")
            fan_in = h.get("fanIn", 0)
            fan_out = h.get("fanOut", 0)
            total = h.get("totalCoupling", 0)
            depends_on = ", ".join(h.get("dependsOn", [])[:3])
            depended_by = ", ".join(h.get("dependedBy", [])[:3])
            if len(h.get("dependsOn", [])) > 3:
                depends_on += "..."
            if len(h.get("dependedBy", [])) > 3:
                depended_by += "..."
            lines.append(f"| `{cls_name}` | {cls_type} | {fan_in} | {fan_out} | {total} | {depends_on} | {depended_by} |")

    # --- Complex Methods (pinpointed per-method) ---
    complex_methods = impact_report.get("complexMethods", [])
    if complex_methods:
        lines.append(f"\n### 🧩 High Complexity Methods ({len(complex_methods)} methods above threshold):")
        lines.append("| Class | Method | Complexity | Severity | File |")
        lines.append("|-------|--------|------------|----------|------|")
        for cm in complex_methods:
            cls = cm.get("className", "?")
            method = cm.get("methodName", "?")
            cpx = cm.get("complexity", 0)
            severity = cm.get("severity", "?")
            filepath = cm.get("filePath", "?")
            # Show only filename, not full path
            short_path = filepath.split("/")[-1] if "/" in filepath else filepath.split("\\")[-1] if "\\" in filepath else filepath
            lines.append(f"| `{cls}` | `{method}()` | {cpx} | {severity} | {short_path} |")

    # --- Class Details (top by dependency count) ---
    class_details = impact_report.get("classDetails", [])
    if class_details:
        lines.append(f"\n### 📦 Class Overview (Top {len(class_details)} by dependency count):")
        lines.append("| Class | Type | Package | Dependencies | Methods |")
        lines.append("|-------|------|---------|-------------|---------|")
        for cd in class_details:
            cls = cd.get("className", "?")
            cls_type = cd.get("type", "?")
            pkg = cd.get("packageName", "")
            dep_count = cd.get("dependencyCount", 0)
            method_count = cd.get("methodCount", 0)
            deps = cd.get("dependencies", [])
            dep_str = ", ".join(deps[:4])
            if len(deps) > 4:
                dep_str += f"... (+{len(deps)-4} more)"
            lines.append(f"| `{cls}` | {cls_type} | {pkg} | {dep_count}: {dep_str} | {method_count} |")

    # --- Dependency Graph Overview ---
    if violations:
        lines.append("\n### Additional Violations from Webhook:")
        for v in violations:
            lines.append(f"- {v}")

    node_count = len(graph_data.get("nodes", []))
    edge_count = len(graph_data.get("edges", []))
    lines.append(f"\n### Dependency Graph: {node_count} nodes, {edge_count} edges")

    # Show edge list for graph understanding
    edges = graph_data.get("edges", [])
    if edges:
        lines.append("Key dependencies:")
        for edge in edges[:20]:
            lines.append(f"  `{edge.get('from', '?')}` → `{edge.get('to', '?')}` ({edge.get('type', 'DEPENDS_ON')})")
        if len(edges) > 20:
            lines.append(f"  ... and {len(edges) - 20} more edges")

    return "\n".join(lines)


def parse_llm_response(content: str) -> Dict[str, Any]:
    explanation = content
    suggestions = []

    lines = content.split("\n")
    in_recommendations = False
    for line in lines:
        stripped = line.strip()
        if any(keyword in stripped.lower() for keyword in ["recommendation", "suggestion", "action item", "next step"]):
            in_recommendations = True
            continue
        if in_recommendations and stripped.startswith(("-", "*", "1.", "2.", "3.", "4.", "5.", "6.", "7.", "8.", "9.", "10.")):
            suggestion = stripped.lstrip("-*0123456789. ").strip()
            if suggestion:
                suggestions.append(suggestion)

    return {
        "explanation": explanation,
        "suggestions": suggestions if suggestions else ["Review the detected violations and apply architectural best practices."],
    }


async def call_llm(system_prompt: str, user_prompt: str) -> str:
    if not OPENROUTER_API_KEY:
        return generate_rule_based_response(user_prompt)

    payload = {
        "model": LLM_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        "max_tokens": 2048,
        "temperature": 0.3,
    }

    headers = {
        "Authorization": f"Bearer {OPENROUTER_API_KEY}",
        "Content-Type": "application/json",
        "HTTP-Referer": "https://github.com/Prabal0202/ArchitectureDoctor",
        "X-Title": "ArchSentinel AI",
    }

    async with httpx.AsyncClient(timeout=30.0) as client:
        response = await client.post(OPENROUTER_API_URL, json=payload, headers=headers)
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"]


def generate_rule_based_response(user_prompt: str) -> str:
    """
    Generates a focused, non-repetitive AI review when no LLM API key is available.
    Follows the same headers as the LLM prompt template.
    """
    lines = []

    violations = _extract_section_items(user_prompt, "Layer Violations")
    risks = _extract_section_items(user_prompt, "Performance Risks")
    circular = _extract_section_items(user_prompt, "Circular Dependencies")
    hotspot_lines = _extract_table_rows(user_prompt, "Coupling Hotspots")
    complex_lines = _extract_table_rows(user_prompt, "High Complexity Methods")

    issue_count = len(violations) + len(risks) + len(circular)

    # --- What's working well ---
    lines.append("### What's working well\n")
    if issue_count == 0 and not circular:
        lines.append("Your architecture is clean — no layer violations, no circular dependencies, "
                      "and no critical performance issues. Well-structured codebase. 🎉\n")
    else:
        positives = []
        if not violations:
            positives.append("All classes follow the **Controller → Service → Repository** pattern correctly")
        if not circular:
            positives.append("No circular dependencies — classes can be tested and deployed independently")
        if len(risks) <= 2:
            positives.append("Minimal performance risk exposure")
        for p in positives:
            lines.append(f"- ✅ {p}")
        if not positives:
            lines.append("- The codebase has clear separation into recognizable layers")
        lines.append("")

    # --- What needs attention (max 3, specific) ---
    attention_items = []
    for v in violations[:2]:
        clean = v.replace("LAYER_VIOLATION: ", "")
        attention_items.append(
            f"**{clean}** — This shortcut means DB schema changes will force controller changes too. "
            f"Create a dedicated Service class between them."
        )
    for c in circular[:1]:
        attention_items.append(
            f"**Circular dependency: `{c}`** — These classes can't be tested in isolation. "
            f"Extract an interface for one side to break the cycle."
        )
    for r in risks[:1]:
        clean = r.replace("PERFORMANCE_RISK: Potential N+1 query in ", "").replace("PERFORMANCE_RISK: ", "")
        attention_items.append(
            f"**`{clean}`** — DB call inside a loop. At 1K records = 1K queries. "
            f"Pre-fetch all needed data before the loop."
        )

    if attention_items:
        lines.append("### What needs attention\n")
        for item in attention_items[:3]:
            lines.append(f"{item}\n")

    # --- Recommended next steps ---
    lines.append("### Recommended next steps\n")
    step = 1
    if violations:
        lines.append(f"{step}. Add Service layer where controllers access repositories directly ({len(violations)} location(s))")
        step += 1
    if circular:
        lines.append(f"{step}. Break circular dependencies with interfaces ({len(circular)} cycle(s))")
        step += 1
    if risks:
        lines.append(f"{step}. Replace loop-based DB calls with batch queries ({len(risks)} location(s))")
        step += 1
    high_coupling = [h for h in hotspot_lines if _get_table_col(h, 4, 0) > 5]
    if high_coupling:
        cls_name = _get_table_col_str(high_coupling[0], 0, "?")
        lines.append(f"{step}. Split `{cls_name}` into smaller focused classes")
        step += 1
    critical = [c for c in complex_lines if "CRITICAL" in c or "HIGH" in c]
    if critical:
        cls_method = _get_table_col_str(critical[0], 0, "?") + "." + _get_table_col_str(critical[0], 1, "?")
        lines.append(f"{step}. Refactor `{cls_method}` — extract helper methods to reduce branches")
        step += 1
    if step == 1:
        lines.append("1. ✅ Architecture looks solid — add integration tests to protect these boundaries")

    return "\n".join(lines)


def _get_table_col_str(row: str, col_index: int, default: str = "") -> str:
    """Get a string value from a table column."""
    try:
        cols = [c.strip().strip('`') for c in row.split("|") if c.strip()]
        return cols[col_index]
    except (IndexError, ValueError):
        return default


# ---- Helper functions for parsing the user prompt ----

def _extract_section_items(text: str, section_name: str) -> List[str]:
    """Extract bullet items from a section in the prompt."""
    items = []
    in_section = False
    for line in text.split("\n"):
        if section_name in line and ("###" in line or "**" in line):
            in_section = True
            continue
        if in_section:
            stripped = line.strip()
            if stripped.startswith("- `") or stripped.startswith("- LAYER") or stripped.startswith("- PERFORMANCE"):
                items.append(stripped.lstrip("- ").strip().strip('`'))
            elif stripped.startswith("###") or (stripped.startswith("**") and not stripped.startswith("**New")):
                break
    return items


def _extract_table_rows(text: str, section_name: str) -> List[str]:
    """Extract table data rows (skip header and separator) from a section."""
    rows = []
    in_section = False
    skip_count = 0
    for line in text.split("\n"):
        if section_name in line:
            in_section = True
            skip_count = 0
            continue
        if in_section:
            stripped = line.strip()
            if stripped.startswith("|"):
                skip_count += 1
                if skip_count > 2:
                    rows.append(stripped)
            elif stripped.startswith("###") or (stripped == "" and skip_count > 2):
                if rows:
                    break
    return rows


def _find_line(text: str, keyword: str) -> str:
    """Find a line containing the keyword."""
    for line in text.split("\n"):
        if keyword in line:
            return line.strip().strip("*")
    return ""


def _extract_between(text: str, start: str, end: str) -> str:
    """Extract text between two markers."""
    try:
        s = text.index(start) + len(start)
        e = text.index(end, s)
        return text[s:e]
    except (ValueError, IndexError):
        return ""


def _get_table_col(row: str, col_index: int, default: int = 0) -> int:
    """Get an integer value from a table column."""
    try:
        cols = [c.strip().strip('`') for c in row.split("|") if c.strip()]
        return int(cols[col_index])
    except (IndexError, ValueError):
        return default


async def analyze(impact_report: Dict[str, Any], graph_data: Dict[str, Any], violations: List[str]) -> Dict[str, Any]:
    system_prompt = load_system_prompt()
    user_prompt = build_user_prompt(impact_report, graph_data, violations)

    try:
        llm_response = await call_llm(system_prompt, user_prompt)
        parsed = parse_llm_response(llm_response)
        risk_level = impact_report.get("riskLevel", impact_report.get("risk_level", "UNKNOWN"))
        return {
            "explanation": parsed["explanation"],
            "suggestions": parsed["suggestions"],
            "riskAssessment": risk_level,
        }
    except Exception as e:
        return {
            "explanation": f"AI analysis unavailable: {str(e)}. Please review the metrics manually.",
            "suggestions": ["Review architectural metrics and address violations manually."],
            "riskAssessment": impact_report.get("riskLevel", "UNKNOWN"),
        }
