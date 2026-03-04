<div align="center">

# 🏗️ ArchSentinel AI — PR-Based Architecture Impact Analyzer

### *Your Codebase Guardian — Automated Architecture Health Checks on Every Pull Request*

[![Java](https://img.shields.io/badge/Java-Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![React](https://img.shields.io/badge/React-Vite-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](#-license)

<br/>

<img src="docs/screenshots/Screenshot%202026-03-04%20134629.png" alt="ArchSentinel AI — Hero" width="90%"/>

<br/>

> *"Is this PR degrading our architecture?" — just push, and ArchSentinel will tell you.*

</div>

---

## 📸 Screenshots

<div align="center">
<table>
  <tr>
    <td align="center"><img src="docs/screenshots/Screenshot%202026-03-04%20134221.png" alt="Dashboard Overview" width="400"/><br/><b>Dashboard Overview</b></td>
    <td align="center"><img src="docs/screenshots/Screenshot%202026-03-04%20134239.png" alt="Token Registration" width="400"/><br/><b>Token Registration</b></td>
  </tr>
  <tr>
    <td align="center" colspan="2"><img src="docs/screenshots/Screenshot%202026-03-04%20134321.png" alt="PR Analysis Report" width="600"/><br/><b>PR Analysis Report</b></td>
  </tr>
</table>
</div>

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 👥 **Multi-User Support** | Any user registers their GitHub token via web UI or API — no server restart needed |
| 🔄 **Automated PR Analysis** | Triggered via GitHub webhooks on PR open / update / push events |
| 🔍 **Static Code Analysis** | JavaParser-based AST parsing for Java projects |
| 🕸️ **Dependency Graph** | Builds directed dependency graphs + Mermaid diagrams |
| 📊 **Coupling Analysis** | Detects classes that know too many others |
| 🔄 **Circular Dependency Detection** | DFS with coloring to find cycles |
| 🧩 **Complexity Analysis** | Flags hard-to-read methods |
| 🏛️ **Layer Violation Detection** | Catches Controller → Repository direct calls |
| ⚡ **Performance Risk Detection** | Identifies real N+1 query patterns |
| 💯 **Health Score** | Weighted 0–100 score across all metrics |
| 🤖 **AI Analysis** | LLM-powered explanations via OpenRouter API |
| 💬 **PR Comments** | Beautiful architecture reports posted directly on GitHub PRs |

---

## 🏛️ Architecture

```
Any GitHub User
       │
       ├── 1. Register token via Web UI or API
       │
       ├── 2. Add webhook on their repo → /webhook/github
       │
       └── 3. Open a PR → ArchSentinel analyzes & posts comment
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│  archsentinel-backend (Spring Boot :8080)                   │
│                                                             │
│  InstallationTokenStore ──── Maps repo → user's token       │
│  WebhookController                                          │
│       │                                                     │
│       ├── RepoCloner (JGit)                                 │
│       ├── StaticAnalyzer (JavaParser)                        │
│       ├── GraphBuilder ──── DependencyGraph                  │
│       ├── MetricsEngine ──── All metrics                     │
│       ├── HealthScoreCalculator ──── 0-100 score             │
│       ├── DiffEngine ──── Base vs PR comparison              │
│       ├── AiServiceClient ──► ai-service (FastAPI :8000)     │
│       └── GitHubCommentService ──── Posts PR comment          │
└─────────────────────────────────────────────────────────────┘
                        │
                        ▼
         ┌──────────────────────────┐
         │  ai-service (FastAPI)    │
         │  OpenRouter LLM ──► GPT  │
         └──────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Docker** & **Docker Compose**
- **OpenRouter API Key** *(optional, for AI analysis)*

### 1️⃣ Clone the repository

```bash
git clone https://github.com/Prabal864/ArchSentinel.git
cd ArchSentinel
```

### 2️⃣ Configure environment variables

```bash
export WEBHOOK_SECRET=your_random_secret          # Webhook signature validation
export OPENROUTER_API_KEY=your_openrouter_key      # AI analysis (optional)
export GITHUB_TOKEN=ghp_fallback_token             # Fallback token (optional)
```

### 3️⃣ Start with Docker Compose

```bash
docker-compose up --build
```

### 4️⃣ Open the Web UI

Navigate to **`http://localhost:8080`** — register your token and start analyzing PRs!

---

## 👤 How It Works (Multi-User)

<div align="center">

```
Register Token  ──►  Add Webhook  ──►  Open a PR  ──►  Get Report! 🎉
```

</div>

**Step 1: Register Your Token**
- Open the web UI at `http://your-server:8080`
- Paste your [GitHub Personal Access Token](https://github.com/settings/tokens/new?scopes=repo&description=ArchSentinel) (needs `repo` scope)
- Click **"Connect All My Repos"** — it auto-discovers all your repos

**Step 2: Add Webhook on Your Repo**
1. Go to your repo → **Settings → Webhooks → Add webhook**
2. **Payload URL**: `https://your-server.com/webhook/github`
3. **Content type**: `application/json`
4. **Events**: Select `Pull requests` + `Pushes`

**Step 3: Open a PR → Get Report!**
Every PR will automatically receive an architecture health report as a comment. ✅

---

## 📡 API Endpoints

### User Registration

| Method | Path | Description |
|:------:|------|-------------|
| `POST` | `/api/register` | Register a GitHub token |
| `DELETE` | `/api/register/{owner}/{repo}` | Remove token for a repo |
| `GET` | `/api/repos` | List all registered repos (tokens masked) |
| `GET` | `/api/health` | System health + stats |

### Webhook

| Method | Path | Description |
|:------:|------|-------------|
| `POST` | `/webhook/github` | GitHub webhook receiver |
| `GET` | `/webhook/test` | Test if backend is running |

### AI Service

| Method | Path | Description |
|:------:|------|-------------|
| `POST` | `/analyze` | Analyze architecture impact |
| `GET` | `/health` | Health check |

---

## 📊 Scoring Model

<div align="center">

```
Health Score = 100 - (
    Coupling     × 0.25  +
    Circular     × 0.25  +
    Complexity   × 0.20  +
    Performance  × 0.20  +
    Layer        × 0.10
)
```

| Score Range | Risk Level | Status |
|:-----------:|:----------:|:------:|
| 80 – 100 | 🟢 HEALTHY | All clear |
| 60 – 79 | 🟡 MODERATE | Needs attention |
| 40 – 59 | 🟠 AT_RISK | Refactoring recommended |
| 0 – 39 | 🔴 CRITICAL | Immediate action needed |

</div>

---

## ⚙️ Configuration

| Variable | Description | Default |
|----------|-------------|:-------:|
| `GITHUB_TOKEN` | Fallback token (if no user token registered) | — |
| `WEBHOOK_SECRET` | GitHub Webhook HMAC secret | — |
| `OPENROUTER_API_KEY` | OpenRouter API key for LLM | — |
| `LLM_MODEL` | LLM model to use | `openai/gpt-4o-mini` |
| `AI_SERVICE_URL` | URL of the AI service | `http://ai-service:8000` |
| `CLONE_DIR` | Directory for repo clones | `/tmp/archsentinel-repos` |

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

## 🛠️ Tech Stack

<div align="center">

| Layer | Technology |
|:-----:|:----------:|
| **Frontend** | React + Vite |
| **Backend** | Java 17, Spring Boot |
| **AI Service** | Python, FastAPI |
| **Code Parsing** | JavaParser (AST) |
| **AI/LLM** | OpenRouter API |
| **Containerization** | Docker, Docker Compose |
| **Version Control** | Git, JGit |

</div>

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

---

<div align="center">


*If you found this useful, give it a ⭐ on GitHub!*

</div>

