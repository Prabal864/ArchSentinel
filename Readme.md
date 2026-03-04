# 🏗️ ArchSentinel AI – PR-Based Backend Architecture Impact Analyzer

> Automatically analyze GitHub Pull Requests for architectural degradation, compute health scores, and get AI-powered recommendations — **supports multiple users & repos**.

<p align="center">
  <img src="docs/screenshots/Screenshot%202026-03-04%20134629.png" alt="Main Website Hero Section" width="800" />
</p>

## ✨ Features

- **Multi-User Support** – Any user registers their GitHub token via web UI or API — no server restart needed
- **Automated PR Analysis** – Triggered via GitHub webhooks on PR open/update/push events
- **Static Code Analysis** – JavaParser-based AST parsing for Java projects
- **Dependency Graph** – Builds directed dependency graphs + Mermaid diagrams
- **Architecture Metrics**:
  - 📊 Coupling Analysis (which classes know too many others)
  - 🔄 Circular Dependency Detection (DFS with coloring)
  - 🧩 Complexity Analysis (hard-to-read methods)
  - 🏛️ Layer Violation Detection (Controller→Repository direct calls)
  - ⚡ Performance Risk Detection (real N+1 query patterns)
- **Health Score** – Weighted 0–100 score across all metrics
- **AI Analysis** – LLM-powered explanation via OpenRouter API
- **PR Comments** – Beautiful reports posted directly on GitHub PRs

---

## 🏛️ Architecture

```
Any GitHub User
       │
       ├── 1. Register token via Web UI (http://your-server:8080)
       │        or API: POST /api/register { "token": "ghp_xxx" }
       │
       ├── 2. Add webhook on their repo → https://your-server/webhook/github
       │
       └── 3. Open a PR → ArchSentinel analyzes & posts comment
              │
              ▼
archsentinel-backend (Spring Boot :8080)
  InstallationTokenStore ──── Maps repo → user's token
  WebhookController
       │
       ├── Resolve token for this repo (user token → env fallback)
       ├── RepoCloner (JGit) ──── Clone with user's token
       ├── StaticAnalyzer (JavaParser) ──── Parse .java files
       ├── GraphBuilder ──── Build DependencyGraph
       ├── MetricsEngine ──── All metrics
       ├── HealthScoreCalculator ──── Weighted 0-100 score
       ├── DiffEngine ──── Compare base vs PR snapshots
       ├── AiServiceClient ──► ai-service (FastAPI :8000) ──► OpenRouter LLM
       └── GitHubCommentService ──── Post PR comment with user's token
```

---

## 👤 How Users Use This (Multi-User)

### For Each User:

**Step 1: Register Your Token**
- Open `http://your-server:8080` in your browser
- Paste your [GitHub Personal Access Token](https://github.com/settings/tokens/new?scopes=repo&description=ArchSentinel) (needs `repo` scope)
- Click "Connect All My Repos" — it auto-discovers all your repos

Or via API:
```bash
# Auto-discover all repos
curl -X POST http://your-server:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"token": "ghp_your_token"}'

# Or register for a specific repo
curl -X POST http://your-server:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"token": "ghp_your_token", "repo": "owner/repo"}'
```

**Step 2: Add Webhook on Your Repo**
1. Go to your repo → **Settings → Webhooks → Add webhook**
2. **Payload URL**: `https://your-server.com/webhook/github`
3. **Content type**: `application/json`
4. **Secret**: _(same as server's WEBHOOK_SECRET, or leave empty)_
5. **Events**: Select `Pull requests` + `Pushes`

**Step 3: Open a PR → Get Report!**
That's it. Every PR will get an architecture health report as a comment.

---

## 🚀 Quick Start (Server Setup)

### Prerequisites
- Docker & Docker Compose
- OpenRouter API Key (optional, for AI analysis)

### 1. Clone the repository
```bash
git clone https://github.com/Prabal0202/ArchitectureDoctor.git
cd ArchitectureDoctor
```

### 2. Configure environment variables
```bash
# Required for webhook signature validation (optional — can be empty)
export WEBHOOK_SECRET=your_random_secret

# Optional: AI analysis via OpenRouter
export OPENROUTER_API_KEY=your_openrouter_key

# Optional: fallback token for all repos (if no user token registered)
export GITHUB_TOKEN=ghp_fallback_token
```

### 3. Start with Docker Compose
```bash
docker-compose up --build
```

### 4. Open the web UI
Go to `http://localhost:8080` — users can register their tokens here.

---

## 📡 API Endpoints

### User Registration

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/register` | Register a GitHub token (`{"token": "ghp_xxx"}` or `{"token": "ghp_xxx", "repo": "owner/repo"}`) |
| `DELETE` | `/api/register/{owner}/{repo}` | Remove token for a repo |
| `GET` | `/api/repos` | List all registered repos (tokens masked) |
| `GET` | `/api/health` | System health + stats |

### Webhook

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/webhook/github` | GitHub webhook receiver |
| `GET` | `/webhook/test` | Test if backend is running |

### AI Service (FastAPI)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/analyze` | Analyze architecture impact |
| `GET` | `/health` | Health check |

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GITHUB_TOKEN` | Fallback token (used if no user token registered) | _(empty)_ |
| `WEBHOOK_SECRET` | GitHub Webhook HMAC secret | _(empty)_ |
| `OPENROUTER_API_KEY` | OpenRouter API key for LLM | _(empty — uses rule-based fallback)_ |
| `LLM_MODEL` | LLM model to use | `openai/gpt-4o-mini` |
| `AI_SERVICE_URL` | URL of the AI service | `http://ai-service:8000` |
| `CLONE_DIR` | Directory for repo clones | `/tmp/archsentinel-repos` |

---

## 📊 Scoring Model

```
healthScore = 100 - (
    couplingScore    × 0.25  +
    circularScore    × 0.25  +
    complexityScore  × 0.20  +
    performanceScore × 0.20  +
    layerScore       × 0.10
)
```

| Score Range | Risk Level |
|-------------|------------|
| 80–100 | 🟢 HEALTHY |
| 60–79 | 🟡 MODERATE |
| 40–59 | 🟠 AT_RISK |
| 0–39 | 🔴 CRITICAL |

---

## 🔍 How It Works

```
User registers token → POST /api/register
User adds webhook on GitHub repo

GitHub sends webhook → POST /webhook/github
  1. Signature verified
  2. Token resolved: user's PAT (per-repo) → env fallback
  3. Both branches cloned using that token
  4. All .java files parsed (AST)
  5. Dependency graph built
  6. Metrics computed (coupling, complexity, circular deps, violations, perf risks)
  7. Health score calculated
  8. AI service called for analysis
  9. PR comment posted using user's token
```

---

## 🧪 Running Tests

```bash
cd archsentinel-backend
mvn test
```

---

## 🔨 Manual Build

### Backend
```bash
cd archsentinel-backend
mvn clean package
java -jar target/archsentinel-backend-*.jar
```

### AI Service
```bash
cd ai-service
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

MIT License
