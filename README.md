# SolveFlow - Problem Solving Flowchart Program

SolveFlow is an interactive problem-solving flowchart and diagnostic decision tree application built with modern Kotlin and Jetpack Compose. It allows engineers, IT technicians, support teams, and students to model troubleshooting trees, interactively walk through diagnostic decisions to find solutions, export flowcharts as self-contained HTML or vector PDF documents, and package APKs via GitHub Actions.

## ✨ Features

- **Interactive Diagnostic Solver**: Walk step-by-step through any problem solving flowchart with decision prompts, branch choices, and an audit trail log.
- **Visual Flowchart Canvas**: Render and inspect node diagrams with distinct status badges (Start, Decision, Action, Resolved, Escalation).
- **Flowchart Builder & Editor**: Create and modify custom flowcharts, add decision nodes, actions, and conditional branches.
- **Built-in Diagnostic Templates**:
  - *Network Connectivity Diagnostics* (DNS, IP, Router, Gateway, ISP)
  - *Production Service Crash & High Latency* (Logs, Memory, Deadlock, Rollback)
  - *5-Whys Root Cause Analysis (RCA)* (Systemic fault investigation)
  - *Hardware Power & Battery Diagnostics* (Power rails, charger, reset, board)
  - *Critical Customer Incident Triage* (SLA severity, workarounds, hotfixes)
- **Multi-Format Export**:
  - **HTML Export**: Standalone, responsive HTML5 document with embedded SVG vector flowchart diagram, printable CSS, and full decision matrix.
  - **PDF Export**: Formatted vector document generated directly on-device using Android `PdfDocument`.
  - Native Android Share Sheet integration to send via Email, Drive, Slack, etc.
- **GitHub Push & Actions APK Packaging Capable**:
  - Includes `.github/workflows/build_apk.yml` and `.github/workflows/android.yml` pre-configured with `actions/setup-java@v5` (JDK 21) and `actions/setup-node@v4` (Node 24).
  - Automatically packages and publishes debug APK artifacts on push, pull request, or manual trigger (`workflow_dispatch`).

## 🚀 GitHub Actions Setup & Push

To push this repository to GitHub and generate APK packages automatically:

```bash
git init
git add .
git commit -m "Initial commit: SolveFlow Problem Solving Flowchart Program"
git branch -M main
git remote add origin https://github.com/<YOUR-USERNAME>/<YOUR-REPO>.git
git push -u origin main
```

Once pushed:
1. Navigate to the **Actions** tab on GitHub.
2. Select **Build & Package Android APK**.
3. Download the compiled APK artifact from the workflow run!
