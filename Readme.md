<div align="center">

# 🏗️ ArchSentinel AI

### **Your Codebase's Architecture Guardian — AI-Powered Structural Health Analysis on Every Pull Request**


[![Java](https://img.shields.io/badge/Java-Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-Vite-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-Custom-blue?style=for-the-badge)](#-license)
[![Live Demo](https://img.shields.io/badge/🌐_Live_Demo-archsentinel.netlify.app-00C7B7?style=for-the-badge)](https://archsentinel.netlify.app)

<br/>

<img src="docs/screenshots/Screenshot%202026-03-04%20134629.png" alt="ArchSentinel AI — Hero" width="100%"/>

<br/>


<br/>

> *"Is this PR degrading our architecture?"*
> *Just push — and ArchSentinel will tell you.*

<br/>

**Stop merging code that silently erodes your architecture.**
ArchSentinel AI is an automated, AI-powered architecture impact analyzer that integrates seamlessly into your GitHub workflow. Every time a pull request is opened, updated, or pushed to, ArchSentinel performs a deep, multi-dimensional structural analysis of your entire codebase and posts a beautifully formatted, actionable health report — directly as a comment on your PR.

No dashboards to check. No CLI tools to run. No manual reviews to schedule.
Just push, and your architecture speaks for itself.

</div>

---

## 📋 Table of Contents

- [Why ArchSentinel?](#-why-archsentinel)
- [How It Works](#-how-it-works)
- [Core Analysis Capabilities](#-core-analysis-capabilities)
  - [Coupling Analysis](#-coupling-analysis)
  - [Circular Dependency Detection](#-circular-dependency-detection)
  - [Cyclomatic Complexity Analysis](#-cyclomatic-complexity-analysis)
  - [Layer Violation Detection](#%EF%B8%8F-layer-violation-detection)
  - [Performance Risk Detection](#-performance-risk-detection)
- [Health Score Model](#-health-score-model)
- [AI-Powered Explanations & Suggestions](#-ai-powered-explanations--suggestions)
- [PR Comment Reports](#-pr-comment-reports--zero-friction-feedback)
- [Multi-User & Multi-Repo Support](#-multi-user--multi-repo-support)
- [Architecture & Tech Stack](#%EF%B8%8F-architecture--tech-stack)
- [Analysis Pipeline Deep Dive](#-analysis-pipeline-deep-dive)
- [Quick Start](#-quick-start)
- [Configuration Reference](#%EF%B8%8F-configuration-reference)
- [REST API Reference](#-rest-api-reference)
- [Security & Privacy](#-security--privacy)
- [Who Is This For?](#-who-is-this-for)
- [Requirements](#-requirements)
- [Comparison with Alternatives](#-comparison-with-alternatives)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Why ArchSentinel?

Software architecture doesn't break overnight — it **decays incrementally**, one pull request at a time. A shortcut here, a direct dependency there, and before you know it, your clean layered architecture has become an untestable, undeployable monolith.

Traditional code review catches syntax errors and logic bugs. **ArchSentinel catches the structural rot that code review misses.**

### The Problem

| Without ArchSentinel | With ArchSentinel |
|:---------------------|:------------------|
| Architecture violations slip through code review unnoticed | Every PR is automatically analyzed for structural impact |
| Circular dependencies grow silently until deployment breaks | Cycle chains are detected and reported instantly |
| Coupling hotspots accumulate until refactoring becomes a multi-sprint effort | Over-connected classes are flagged before they become entrenched |
| Performance anti-patterns (N+1 queries) reach production | Static detection at review time — before runtime |
| Layer violations erode separation of concerns gradually | Controller → Repository bypasses are caught immediately |
| "Architecture review" is an ad-hoc, inconsistent process | Every single PR gets a quantified, repeatable, AI-explained health check |

### The ArchSentinel Difference

- **🤖 Fully Automated** — no manual triggers, no CLI commands, no dashboards to poll
- **📍 In-Context Feedback** — reports appear exactly where your team works: on the PR itself
- **🔢 Quantified Results** — a single 0–100 health score with weighted, configurable dimensions
- **🧠 AI-Explained** — not just metrics, but LLM-powered explanations of *what they mean* and *what to do about them*
- **👥 Multi-User Native** — any team member registers their own token; no centralized admin bottleneck
- **🔒 Secure by Design** — HMAC webhook validation, scoped tokens, masked credentials in all responses

---

## ⚙️ How It Works

<div align="center">

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
│  Developer   │────▶│  Opens a PR  │────▶│   GitHub      │────▶│  ArchSentinel    │
│  pushes code │     │  or pushes   │     │   sends       │     │  receives        │
│              │     │  to branch   │     │   webhook     │     │  webhook event   │
└─────────────┘     └──────────────┘     └──────────────┘     └────────┬─────────┘
                                                                       │
                                                                       ▼
                                                          ┌────────────────────────┐
                                                          │  8-Step Analysis       │
                                                          │  Pipeline Executes     │
                                                          │                        │
                                                          │  1. Clone base branch  │
                                                          │  2. Clone head branch  │
                                                          │  3. AST parse (both)   │
                                                          │  4. Build dep graphs   │
                                                          │  5. Compute 5 metrics  │
                                                          │  6. Calculate scores   │
                                                          │  7. Generate diff      │
                                                          │  8. AI analysis        │
                                                          └────────────┬───────────┘
                                                                       │
                                                                       ▼
                                                          ┌────────────────────────┐
                                                          │  📝 Posts detailed     │
                                                          │  architecture report   │
                                                          │  as a PR comment       │
                                                          └────────────────────────┘
```

</div>

**In plain English:**

1. A developer opens a pull request (or pushes new commits to an existing one).
2. GitHub fires a webhook event to your ArchSentinel instance.
3. ArchSentinel validates the webhook signature (HMAC-SHA256) for security.
4. The analysis pipeline kicks off **asynchronously** — your PR is never blocked.
5. Both the **base branch** and the **PR head** are cloned at their exact commits.
6. Full static analysis runs on both snapshots independently.
7. A before/after comparison generates a detailed **Impact Report**.
8. An AI service generates human-readable explanations and actionable suggestions.
9. A beautifully formatted architecture report is **posted directly on the PR** as a comment.

**The entire process runs in the background. The developer simply opens a PR and waits for the report to appear — typically within seconds.**

---

## 🔍 Core Analysis Capabilities

ArchSentinel runs **five independent analysis engines** on every pull request. Each engine examines a different dimension of architectural quality, and together they provide a 360-degree view of your codebase's structural health.

---

### 📊 Coupling Analysis

**What it detects:** Classes that are over-connected — the "God objects" that know too much about the rest of your system.

**How it works:**

ArchSentinel measures **fan-in** (how many other classes depend on this one) and **fan-out** (how many other classes this one depends on) for every class in your codebase. Classes that exceed configurable thresholds are flagged as **coupling hotspots**.

**Why it matters:**

High coupling is one of the strongest predictors of maintenance difficulty. When a single class is connected to dozens of others, modifying it creates a blast radius that can cascade across your entire system. Coupling hotspots are the classes most likely to:

- Cause merge conflicts across teams
- Require coordinated deployments
- Resist safe refactoring
- Generate unexpected side effects during changes

**What the report shows:**

- Per-class coupling scores (fan-in / fan-out)
- Identified coupling hotspots with their exact file locations
- Before/after coupling score comparison to show the PR's impact

---

### 🔄 Circular Dependency Detection

**What it detects:** Circular (cyclic) dependency chains where Class A → Class B → Class C → Class A.

**How it works:**

ArchSentinel builds a full **directed dependency graph** from your codebase's import and usage relationships. It then runs a **depth-first search (DFS) with three-color marking** (white/gray/black) to detect back-edges — the hallmark of cycles in directed graphs. When a cycle is found, the full path is recorded.

**Why it matters:**

Circular dependencies are among the most insidious forms of architectural decay:

- They make individual modules **impossible to test in isolation**
- They create **deployment order problems** — you can't deploy A without B, but B depends on A
- They indicate that **module boundaries are poorly defined**
- They grow exponentially harder to fix the longer they exist
- They often indicate that **responsibilities are leaking** across architectural boundaries

**What the report shows:**

- Total count of circular dependency chains found
- Exact cycle paths (e.g., `OrderService → PaymentService → NotificationService → OrderService`)
- Before/after comparison showing whether the PR introduced new cycles

---

### 🧩 Cyclomatic Complexity Analysis

**What it detects:** Methods with excessive branching logic — the functions that are hard to read, hard to test, and hard to maintain.

**How it works:**

Using **JavaParser AST analysis**, ArchSentinel calculates the cyclomatic complexity of every method in your codebase. Cyclomatic complexity counts the number of **independent execution paths** through a function — each `if`, `else`, `switch case`, `for`, `while`, `catch`, `&&`, and `||` adds a path. Methods exceeding safe thresholds are flagged.

**Why it matters:**

Complex methods are the primary source of:

- **Bugs** — more paths mean more chances for unexpected behavior
- **Testing difficulty** — each path requires its own test case; a method with complexity 20 needs at least 20 tests for full branch coverage
- **Cognitive load** — developers struggle to hold complex methods in their heads
- **Review difficulty** — reviewers are more likely to miss issues in complex code

**What the report shows:**

- List of methods exceeding complexity thresholds
- Per-method complexity scores with class name and file location
- Before/after complexity comparison showing the PR's impact on readability

---

### 🏛️ Layer Violation Detection

**What it detects:** Components that bypass architectural boundaries — for example, a Controller calling a Repository directly, skipping the Service layer.

**How it works:**

ArchSentinel classifies every class into its architectural layer based on annotations, naming conventions, and package structure:

| Layer | Detection Criteria |
|:-----:|:-------------------|
| **Controller** | `@RestController`, `@Controller`, classes ending in `Controller` |
| **Service** | `@Service`, classes ending in `Service` |
| **Repository** | `@Repository`, classes ending in `Repository`, `Dao` |

It then checks all dependency edges against a **layer access matrix**. Violations occur when a higher-level layer directly accesses a lower-level layer without going through the intermediate layer.

**Why it matters:**

Layer violations destroy **separation of concerns** — the foundational principle that makes large codebases manageable:

- Controllers that call repositories directly bypass business logic, validation, and transaction management
- Skipped layers create **hidden coupling** between presentation and persistence
- They make it impossible to **swap implementations** (e.g., changing database) without modifying UI code
- They indicate that the **Service layer is either missing or inadequate**

**What the report shows:**

- Total count of layer violations
- Specific violation descriptions (e.g., "Controller `UserController` directly accesses Repository `UserRepository`")
- Before/after comparison showing whether the PR introduced new violations

---

### ⚡ Performance Risk Detection

**What it detects:** Static patterns that are likely to cause performance issues at runtime — most notably **N+1 query patterns**.

**How it works:**

ArchSentinel analyzes the AST for patterns where repository/database calls occur inside loops, or where lazy-loaded collections are accessed in iteration contexts. These patterns are the most common cause of unexpected database load in production.

**Why it matters:**

N+1 queries are **invisible in development** (where you have 10 records) but **devastating in production** (where you have 10,000):

- A single N+1 pattern can turn a 1-query page load into a 10,001-query page load
- They are the #1 cause of **unexpected database load**
- They often hide behind ORM abstractions and are **invisible in code review**
- Fixing them after deployment requires emergency patches

**What the report shows:**

- Total count of performance risks identified
- Specific risk descriptions with file and method locations
- Before/after comparison showing whether the PR introduced new risks

---

## 💯 Health Score Model

Every analysis produces a single, quantified **Health Score (0–100)** that gives your team an instant, at-a-glance read on the structural quality of the codebase — and how the current PR is affecting it.

<div align="center">

### Scoring Formula

```
Health Score = 100 - (
    Coupling Penalty      × 0.25  +
    Circular Dep Penalty  × 0.25  +
    Complexity Penalty    × 0.20  +
    Performance Penalty   × 0.20  +
    Layer Violation Penalty × 0.10
)
```

### Risk Level Classification

| Score Range | Risk Level | Indicator | Recommended Action |
|:-----------:|:----------:|:---------:|:-------------------|
| **80 – 100** | 🟢 **HEALTHY** | ✅ All clear | Safe to merge — architecture is in good shape |
| **60 – 79** | 🟡 **MODERATE** | ⚠️ Needs attention | Review flagged items — consider addressing before merge |
| **40 – 59** | 🟠 **AT RISK** | 🔶 Refactoring needed | Strongly recommend refactoring before merging |
| **0 – 39** | 🔴 **CRITICAL** | 🚨 Immediate action | Do not merge — severe structural problems detected |

</div>

### Why a Single Score Matters

Engineering teams are drowning in metrics. ArchSentinel deliberately distills five complex analysis dimensions into **one number** so that:

- **Developers** get instant feedback without reading a full report
- **Tech leads** can set health score thresholds for merge gates
- **Managers** can track architecture quality trends over time
- **CI/CD pipelines** can gate deployments on architecture quality (coming soon)

### Configurable Weights

Every team has different priorities. ArchSentinel's scoring weights are **fully configurable** via environment variables or Spring Boot configuration:

```yaml
archsentinel:
  scoring:
    coupling-weight: 0.25
    circular-weight: 0.25
    complexity-weight: 0.20
    performance-weight: 0.20
    layer-violation-weight: 0.10
```

Want to prioritize performance? Increase `performance-weight`. Don't care about layer violations? Set it to `0.0`. The scoring model adapts to your team's values.

---

## 🤖 AI-Powered Explanations & Suggestions

Raw metrics tell you **what** is wrong. AI tells you **why** it matters and **what to do about it**.

### How It Works

After the metrics engine computes the Impact Report, ArchSentinel forwards the complete analysis payload — including dependency graphs, metric deltas, coupling hotspots, complex methods, and layer violations — to an integrated **AI service** powered by [OpenRouter](https://openrouter.ai/) (supporting GPT-4o-mini and other LLMs).

The AI service returns:

| Output | Description |
|:------:|:------------|
| **Explanation** | A plain-English summary of the architecture impact — written for humans, not machines |
| **Suggestions** | A numbered list of specific, actionable refactoring steps with class and method references |

### Example AI Output

> **Explanation:**
> This PR introduces a new `NotificationService` that directly imports `UserRepository`, bypassing the existing `UserService`. This creates a layer violation and increases coupling — `UserRepository` now has 4 dependents instead of 2. Additionally, the `sendBulkNotifications` method has a cyclomatic complexity of 14, largely due to nested conditionals handling different notification channels.
>
> **Suggestions:**
> 1. Inject `UserService` into `NotificationService` instead of `UserRepository` directly — this preserves the service layer boundary
> 2. Extract the channel-specific notification logic in `sendBulkNotifications` into a Strategy pattern (`EmailNotifier`, `SmsNotifier`, `PushNotifier`)
> 3. Consider introducing a `NotificationChannel` interface to decouple the notification dispatch from channel implementations

### Graceful Fallback

When the LLM is unavailable (no API key configured, rate limits, network issues), ArchSentinel **gracefully falls back** to a built-in **rule-based reasoning engine** that still produces meaningful, context-aware recommendations based on the detected metrics. Your PR reports never fail silently.

---

## 💬 PR Comment Reports — Zero-Friction Feedback

ArchSentinel posts a comprehensive, beautifully formatted architecture report **directly as a comment on your pull request**. No context switching. No external dashboards. No separate tools.

### What's in the Report

| Section | Contents |
|:--------|:---------|
| **🏥 Health Score** | Before/after scores with delta (e.g., `82 → 76 (Δ -6)`) |
| **📊 Coupling Analysis** | Per-class fan-in/fan-out, identified hotspots |
| **🔄 Circular Dependencies** | Cycle count, exact cycle paths |
| **🧩 Complexity** | Flagged methods with complexity scores and file locations |
| **🏛️ Layer Violations** | Specific violations with class-to-class references |
| **⚡ Performance Risks** | Identified anti-patterns with method locations |
| **🕸️ Dependency Graph** | Visual Mermaid diagram of class relationships |
| **🤖 AI Explanation** | LLM-generated plain-English analysis |
| **📋 Suggestions** | Numbered, actionable refactoring recommendations |
| **📈 Class Details** | Per-class breakdown (name, type, file, dependencies, method count) |

### Why In-PR Feedback Matters

Research consistently shows that **feedback delivered in context is exponentially more effective** than feedback delivered elsewhere:

- Developers don't need to leave their PR to understand architectural impact
- Reviewers can reference the architecture report during code review
- Discussions about structural quality happen alongside the code that caused them
- Historical reports are preserved in the PR timeline for future reference

---

## 👥 Multi-User & Multi-Repo Support

ArchSentinel is designed for **real teams with real workflows** — not just solo developers.

### How Multi-User Works

```
┌──────────────────────────────────────────────────────────────┐
│                   Multi-User Token Flow                       │
│                                                               │
│  User A registers token ──► Maps to User A's repos           │
│  User B registers token ──► Maps to User B's repos           │
│  User C registers token ──► Maps to User C's repos           │
│                                                               │
│  When a webhook arrives for repo "org/service-x":             │
│  InstallationTokenStore looks up: repo → user's token         │
│  Uses that token for clone + PR comment posting               │
└──────────────────────────────────────────────────────────────┘
```

### Setup Flow (Per User)

| Step | Action | Details |
|:----:|:-------|:--------|
| **1** | **Register Token** | Open the web UI → paste your GitHub PAT → click "Connect All My Repos" |
| **2** | **Auto-Discovery** | ArchSentinel automatically discovers all repositories accessible with your token |
| **3** | **Add Webhook** | Go to your repo → Settings → Webhooks → Add `https://your-server/webhook/github` |
| **4** | **Open a PR** | That's it — the architecture report will appear as a PR comment |

### Key Multi-User Features

- ✅ **No server restarts** — tokens are registered at runtime via API/UI
- ✅ **Token isolation** — each user's token is mapped only to their own repos
- ✅ **Token masking** — API responses never expose full token values
- ✅ **Fallback token** — optional global token for repos without a registered user
- ✅ **Auto-discovery** — registering a token automatically enumerates all accessible repos

---

## 🏛️ Architecture & Tech Stack

ArchSentinel is built as a **three-tier microservice architecture**, containerized with Docker Compose for one-command deployment.

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (React + Vite)                   │
│                                                                   │
│  • Token registration dashboard                                  │
│  • Connected repositories overview                                │
│  • System health monitoring                                       │
│  • Responsive, modern UI                                          │
└─────────────────────────────┬─────────────────────────────────────┘
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              Backend (Java 17 + Spring Boot :8080)                │
│                                                                   │
│  ┌─────────────────────┐  ┌──────────────────────────────────┐   │
│  │ WebhookController   │  │  InstallationTokenStore          │   │
│  │ (POST /webhook/     │  │  Maps repo → user's token        │   │
│  │  github)            │  └──────────────────────────────────┘   │
│  └────────┬────────────┘                                          │
│           │                                                       │
│  ┌────────▼────────────────────────────────────────────────────┐  │
│  │              Analysis Pipeline (Async)                      │  │
│  │                                                             │  │
│  │  RepoCloner ──► StaticAnalyzer ──► GraphBuilder             │  │
│  │       │              │                   │                  │  │
│  │       │              ▼                   ▼                  │  │
│  │       │         JavaParser          DependencyGraph          │  │
│  │       │         (AST Parse)         (Directed Graph)        │  │
│  │       │              │                   │                  │  │
│  │       │              └───────┬───────────┘                  │  │
│  │       │                      ▼                              │  │
│  │       │              MetricsEngine                          │  │
│  │       │              (5 Analysis Engines)                   │  │
│  │       │                      │                              │  │
│  │       │                      ▼                              │  │
│  │       │          HealthScoreCalculator                      │  │
│  │       │          (Weighted 0-100)                           │  │
│  │       │                      │                              │  │
│  │       │                      ▼                              │  │
│  │       │              DiffEngine                             │  │
│  │       │         (Before/After Compare)                      │  │
│  │       │                      │                              │  │
│  │       │              ┌───────┴───────┐                      │  │
│  │       │              ▼               ▼                      │  │
│  │       │      AiServiceClient   GitHubCommentService         │  │
│  │       │      (LLM Analysis)    (Post PR Comment)            │  │
│  └───────┴──────────────────────────────────────────────────┘  │
└─────────────────────────────┬─────────────────────────────────────┘
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                AI Service (Python + FastAPI :8000)                │
│                                                                   │
│  ┌─────────────────────┐  ┌──────────────────────────────────┐   │
│  │ /analyze endpoint   │  │  reasoning_engine.py             │   │
│  │ (POST)              │  │  (Rule-based fallback)           │   │
│  └────────┬────────────┘  └──────────────────────────────────┘   │
│           │                                                       │
│           ▼                                                       │
│  ┌─────────────────────┐                                          │
│  │ OpenRouter API      │                                          │
│  │ (GPT-4o-mini /      │                                          │
│  │  configurable LLM)  │                                          │
│  └─────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────┘
```

### Tech Stack Details

| Layer | Technology | Version | Purpose |
|:-----:|:----------:|:-------:|:--------|
| **Frontend** | React + Vite | Latest | Token registration UI, repo management dashboard |
| **Backend** | Java + Spring Boot | 17 / 3.x | Core analysis engine, webhook processing, API |
| **AI Service** | Python + FastAPI | 3.x | LLM integration, rule-based reasoning fallback |
| **Code Parsing** | JavaParser | Latest | Abstract Syntax Tree generation and traversal |
| **AI/LLM** | OpenRouter API | — | GPT-4o-mini (configurable) for analysis explanations |
| **Version Control** | JGit | Latest | Programmatic repository cloning at specific commits |
| **Containerization** | Docker + Compose | Latest | One-command deployment of entire stack |
| **Webhook Security** | HMAC-SHA256 | — | Cryptographic validation of incoming webhooks |

---

## 🔬 Analysis Pipeline Deep Dive

When a webhook arrives, ArchSentinel executes a rigorous **8-step analysis pipeline** asynchronously. Here's exactly what happens at each step:

### For Pull Request Events (`opened` / `synchronize`)

| Step | Component | Action | Output |
|:----:|:---------:|:-------|:-------|
| **1** | `RepoCloner` | Clone repository at **base branch** commit via JGit | Local copy of base codebase |
| **2** | `RepoCloner` | Clone repository at **PR head** commit via JGit | Local copy of PR codebase |
| **3** | `StaticAnalyzer` | Parse all `.java` files in base into AST via JavaParser | `List<ClassInfo>` for base |
| **4** | `StaticAnalyzer` | Parse all `.java` files in PR head into AST via JavaParser | `List<ClassInfo>` for PR |
| **5** | `GraphBuilder` | Build directed dependency graphs from both class lists | `DependencyGraph` (base + PR) |
| **6** | `MetricsEngine` | Compute all 5 metric categories for both snapshots | `ArchitectureSnapshot` × 2 |
| **7** | `HealthScoreCalculator` + `DiffEngine` | Calculate weighted scores, compare before/after | `ImpactReport` with deltas |
| **8** | `AiServiceClient` + `GitHubCommentService` | Get AI analysis, format and post PR comment | ✅ Report on PR |

### For Push Events

Push events follow the same pipeline but compare **before-commit** vs **after-commit** (instead of base vs head branch). For first pushes to a new branch, ArchSentinel intelligently detects the zero-commit state and analyzes the current snapshot only.

### Automatic Cleanup

After every analysis — regardless of success or failure — ArchSentinel **automatically cleans up** all cloned directories to prevent disk space leaks. This is handled in a `finally` block to guarantee cleanup even during exceptions.

---

## 🚀 Quick Start

### Prerequisites

| Requirement | Required? | Purpose |
|:------------|:---------:|:--------|
| **Docker & Docker Compose** | ✅ Yes | Runs the entire stack |
| **GitHub Personal Access Token** | ✅ Yes | Repository access + PR commenting (needs `repo` scope) |
| **OpenRouter API Key** | ⬜ Optional | Enables AI-powered explanations (falls back to rule-based without it) |

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/Prabal864/ArchSentinel.git
cd ArchSentinel
```

### 2️⃣ Configure Environment Variables

```bash
# Required: Secret for validating incoming webhook signatures
export WEBHOOK_SECRET=your_random_secret_here

# Optional: Enables AI-powered analysis explanations
export OPENROUTER_API_KEY=your_openrouter_api_key

# Optional: Fallback token for repos without a registered user
export GITHUB_TOKEN=ghp_your_fallback_token
```

### 3️⃣ Launch with Docker Compose

```bash
docker-compose up --build
```

This starts three containers:
- **Backend** on port `8080` (Spring Boot)
- **AI Service** on port `8000` (FastAPI)
- **Frontend** served via the backend

### 4️⃣ Register Your Token

1. Open **`http://localhost:8080`** in your browser
2. Paste your [GitHub Personal Access Token](https://github.com/settings/tokens/new?scopes=repo&description=ArchSentinel) (needs `repo` scope)
3. Click **"Connect All My Repos"** — ArchSentinel auto-discovers all accessible repositories

### 5️⃣ Add a Webhook to Your Repo

1. Go to your repository → **Settings** → **Webhooks** → **Add webhook**
2. **Payload URL:** `https://your-server.com/webhook/github`
3. **Content type:** `application/json`
4. **Secret:** Same value as your `WEBHOOK_SECRET`
5. **Events:** Select **Pull requests** and **Pushes**
6. Click **Add webhook**

### 6️⃣ Open a PR — Done! 🎉

Open a pull request on any connected repository. Within seconds, ArchSentinel will post a comprehensive architecture health report as a comment on your PR.

---

## ⚙️ Configuration Reference

### Environment Variables

| Variable | Required | Description | Default |
|:---------|:--------:|:------------|:-------:|
| `WEBHOOK_SECRET` | ✅ | HMAC-SHA256 secret for validating incoming GitHub webhook signatures | — |
| `OPENROUTER_API_KEY` | ⬜ | API key for OpenRouter LLM service (enables AI-powered explanations) | — |
| `GITHUB_TOKEN` | ⬜ | Fallback GitHub token used when no user token is registered for a repo | — |
| `LLM_MODEL` | ⬜ | LLM model identifier to use via OpenRouter | `openai/gpt-4o-mini` |
| `AI_SERVICE_URL` | ⬜ | Internal URL of the AI analysis microservice | `http://ai-service:8000` |
| `CLONE_DIR` | ⬜ | Directory path for temporary repository clones | `/tmp/archsentinel-repos` |

### Scoring Configuration (Spring Boot)

```yaml
archsentinel:
  scoring:
    coupling-weight: 0.25          # Weight for coupling analysis (0.0 – 1.0)
    circular-weight: 0.25          # Weight for circular dependency detection
    complexity-weight: 0.20        # Weight for cyclomatic complexity
    performance-weight: 0.20       # Weight for performance risk detection
    layer-violation-weight: 0.10   # Weight for layer violation detection
```

> **Note:** Weights should sum to 1.0 for a meaningful 0–100 score, but this is not enforced — you can over/under-weight as desired.

---

## 📡 REST API Reference

### Token Registration

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `POST` | `/api/register` | Register a GitHub Personal Access Token. Auto-discovers all accessible repos. | None |
| `DELETE` | `/api/register/{owner}/{repo}` | Remove the registered token for a specific repository. | None |
| `GET` | `/api/repos` | List all registered repositories with masked token values. | None |
| `GET` | `/api/health` | System health check — returns uptime, registered repo count, and system stats. | None |

### Webhook

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `POST` | `/webhook/github` | Receives GitHub webhook events. Validates HMAC signature. Processes `pull_request` and `push` events. | HMAC-SHA256 |
| `GET` | `/webhook/test` | Simple health check to verify the backend is running and accessible. | None |

### AI Service

| Method | Endpoint | Description | Auth |
|:------:|:---------|:------------|:----:|
| `POST` | `/analyze` | Accepts an Impact Report payload and returns AI-generated explanation + suggestions. | None (internal) |
| `GET` | `/health` | Health check for the AI microservice. | None |

---

## 🛡️ Security & Privacy

ArchSentinel is designed with security as a first-class concern:

| Security Feature | Implementation |
|:-----------------|:---------------|
| **Webhook Signature Validation** | Every incoming webhook is validated against `WEBHOOK_SECRET` using HMAC-SHA256. Invalid signatures are rejected with HTTP 401. |
| **Token Isolation** | Each user's GitHub PAT is mapped exclusively to their own repositories. No cross-user token access. |
| **Token Masking** | All API responses that include token information mask the token value. Full tokens are never exposed via the API. |
| **Scoped Access** | ArchSentinel requires only the `repo` scope — the minimum needed for cloning and PR commenting. |
| **Automatic Cleanup** | Cloned repository directories are deleted immediately after analysis, regardless of success or failure. |
| **No Data Persistence** | ArchSentinel does not persist your code, analysis results, or tokens to any database. Everything is in-memory and ephemeral. |
| **Internal AI Communication** | The AI service is internal to the Docker network and not exposed externally by default. |

---

## 🎯 Who Is This For?

### 🏢 Engineering Teams

> "We merge 50+ PRs per week across 12 services. We can't manually review every one for architecture compliance."

ArchSentinel gives your team **automated, consistent architecture governance** on every single PR — no exceptions, no human bottleneck.

### 👨‍💻 Tech Leads & Architects

> "I need to enforce our architecture principles without becoming a review bottleneck."

ArchSentinel codifies your architecture rules into automated checks. Layer violations, coupling thresholds, and complexity limits are enforced programmatically — you only need to review the exceptions.

### 🚀 Solo Developers & Indie Hackers

> "I don't have a team to review my architecture decisions. I need a second pair of eyes."

ArchSentinel acts as your AI-powered architecture advisor — catching mistakes that are invisible when you're deep in implementation mode.

### 🌍 Open Source Maintainers

> "Contributors submit PRs from all over the world. I need to ensure they don't degrade the codebase structure."

ArchSentinel automatically analyzes every contributor PR, giving maintainers a structural quality assessment before they even begin code review.

### 📚 Educators & Students

> "I want to teach students what good architecture looks like — with real, measurable feedback."

ArchSentinel provides quantified, educational feedback on architectural quality — perfect for university courses, bootcamps, and self-study.

---

## 📋 Requirements

| Component | Minimum | Recommended |
|:----------|:--------|:------------|
| **Docker** | 20.10+ | Latest |
| **Docker Compose** | 2.0+ | Latest |
| **Memory** | 512 MB | 2 GB+ |
| **Disk** | 1 GB free | 5 GB+ (for cloning large repos) |
| **GitHub Token** | PAT with `repo` scope | Fine-grained PAT with repository access |
| **OpenRouter Key** | — | Required for AI-powered explanations |

---

## ⚖️ Comparison with Alternatives

| Feature | ArchSentinel AI | SonarQube | CodeClimate | Codacy |
|:--------|:---------------:|:---------:|:-----------:|:------:|
| **PR-native comments** | ✅ | ⬜ Partial | ✅ | ✅ |
| **Architecture-specific analysis** | ✅ | ⬜ Limited | ❌ | ❌ |
| **Circular dependency detection** | ✅ | ❌ | ❌ | ❌ |
| **Layer violation detection** | ✅ | ❌ | ❌ | ❌ |
| **Dependency graph visualization** | ✅ | ❌ | ❌ | ❌ |
| **AI-powered explanations** | ✅ | ❌ | ❌ | ❌ |
| **Single health score (0–100)** | ✅ | ⬜ Different model | ✅ | ✅ |
| **Before/after PR comparison** | ✅ | ⬜ Partial | ⬜ Partial | ⬜ Partial |
| **Multi-user token registration** | ✅ | N/A | N/A | N/A |
| **Self-hosted** | ✅ | ✅ | ❌ | ❌ |
| **Open source** | ✅ | ⬜ Community Edition | ❌ | ❌ |
| **Zero configuration** | ✅ | ❌ | ⬜ Partial | ⬜ Partial |
| **Docker one-command deploy** | ✅ | ⬜ Complex | N/A | N/A |

---

## 🗺️ Roadmap

| Status | Feature | Description |
|:------:|:--------|:------------|
| ✅ | **Core Analysis** | Coupling, circular deps, complexity, layers, performance |
| ✅ | **AI Explanations** | LLM-powered analysis via OpenRouter |
| ✅ | **Multi-User** | Token registration with auto-discovery |
| ✅ | **Push Analysis** | Before/after commit comparison on push events |
| ✅ | **Docker Compose** | One-command deployment |
| 🔜 | **GitHub App** | Native GitHub App installation (no webhook setup needed) |
| 🔜 | **Multi-Language** | Support for Python, TypeScript, Go, Kotlin, and more |
| 🔜 | **Historical Trends** | Track health score over time with charts and dashboards |
| 🔜 | **CI/CD Integration** | Fail builds when health score drops below threshold |
| 🔜 | **Custom Rules** | Define your own architectural rules and patterns |
| 🔜 | **Slack/Discord Notifications** | Alert channels when critical PRs are detected |
| 🔜 | **GitHub Checks API** | Native status checks on PRs (pass/fail) |
| 📋 | **Team Dashboards** | Organization-wide architecture health overview |
| 📋 | **Dependency Graph UI** | Interactive visualization of your architecture |
| 📋 | **PR Merge Gates** | Block PRs that don't meet architecture standards |

---

## 🤝 Contributing

We welcome contributions from the community! Whether it's a bug fix, new analysis engine, language support, or documentation improvement — every contribution matters.

### How to Contribute

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Development Setup

```bash
# Backend
cd backend
mvn clean package
java -jar target/archsentinel-backend-*.jar

# AI Service
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

### Running Tests

```bash
cd backend
mvn test
```

---

## 📜 License

This project is released under a custom license. See the [LICENSE](LICENSE) file for details.

---

<div align="center">

<br/>

**🏗️ ArchSentinel AI**
*Your architecture deserves a guardian. Every PR deserves a health check.*

<br/>

[🌐 Live Demo](https://archsentinel.netlify.app) · [📦 Source Code](https://github.com/Prabal864/ArchSentinel) · [🐛 Report Bug](https://github.com/Prabal864/ArchSentinel/issues) · [💡 Request Feature](https://github.com/Prabal864/ArchSentinel/issues)

<br/>

*If you found this useful, give it a ⭐ on GitHub!*

</div>
