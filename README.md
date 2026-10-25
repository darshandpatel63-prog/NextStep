# NextStep — Offline Everyday Problem Guide

Privacy-first native Android MVP. No AI API, backend, login, or user account.

## Initial guides
- Payment debited, order not confirmed
- Wrong, damaged, or missing delivery
- Application or form rejected
- Suspicious message or payment request

Features: offline step-by-step guides, locally saved checklists, and copyable message templates.

## Build and get the APK
The **Build NextStep APK and publish Release** workflow runs when code is pushed to `main`, or when manually started from the Actions tab. Gradle caching is disabled, and the workflow does not upload Actions artifacts. After a successful build, the APK is attached directly to a GitHub prerelease under **Releases**.

Open the repository's **Releases** page and download `NextStep-debug.apk` from the newest NextStep build. This is a debug APK for testing, not a production-signed release.

## Important
This starter does not include AdMob. Configure a separate AdMob app and ad-unit IDs for NextStep only after testing and reviewing Google policies. Use test ads during development; do not reuse another app's ad IDs.

NextStep is an independent guide, not a bank, government service, legal adviser, or payment provider. Always verify current procedures with official channels. The app does not guarantee refunds, approvals, or any other outcome.
