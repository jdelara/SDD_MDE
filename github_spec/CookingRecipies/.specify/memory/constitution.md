<!--
SYNC IMPACT REPORT
==================
Version change: (unset) → 1.0.0
Initial ratification — all sections newly authored.

Modified principles: N/A (first version)
Added sections: Core Principles, Technology Stack, Development Workflow, Governance
Removed sections: N/A

Templates reviewed:
  ✅ .specify/templates/plan-template.md — Constitution Check gate is generic; no updates needed.
  ✅ .specify/templates/spec-template.md — No principle-driven mandatory sections to add for this scope.
  ✅ .specify/templates/tasks-template.md — Path conventions and phase structure are compatible.
  ✅ .specify/templates/commands/ — No command files present; nothing to update.

Deferred TODOs: none
-->

# CookingRecipies Constitution

## Core Principles

### I. Plain Java & Swing Only

All UI MUST be built exclusively with `javax.swing` components from the Java SE standard library.
No third-party UI frameworks (JavaFX, SWT, Vaadin, etc.) are permitted.
Business logic MUST be kept in plain Java classes with no external runtime dependencies.

### II. Eclipse Project Structure

The project MUST be a valid Eclipse Java project at all times:
- `.project` and `.classpath` files MUST be present and correct.
- Source code lives under `src/`; compiled output goes to `bin/`.
- Eclipse's built-in Java builder is the sole build mechanism — no Maven, Gradle, or Ant unless
  the user explicitly amends this constitution.

### III. Simplicity First (NON-NEGOTIABLE)

YAGNI. The application MUST remain simple:
- No databases, no ORM, no dependency injection containers.
- If data persistence is needed, plain text or serialized files are the only allowed mechanism.
- Abstractions are introduced only when duplication is concrete and present, not anticipated.
- Every added class must justify its existence; reject complexity without a clear requirement.

## Technology Stack

- **Language**: Java 11 or later (use the Eclipse workspace default JDK).
- **UI Toolkit**: Swing (`javax.swing`, `java.awt`) — standard library only.
- **IDE**: Eclipse IDE for Java Developers.
- **Build**: Eclipse built-in builder (no external build tool).
- **Dependencies**: None beyond the Java SE standard library.

## Development Workflow

- All code changes MUST compile cleanly in Eclipse with zero errors and no warnings suppressed.
- Manual testing is the primary verification method: run the main class from Eclipse and exercise
  the feature through the Swing UI.
- Each feature MUST have a single entry point (`main` method in a clearly named class).
- Code style follows standard Java conventions (camelCase methods/fields, PascalCase classes).

## Governance

This constitution supersedes all informal coding practices for this project.
Amendments require:
1. Updating this file with an incremented version number.
2. Documenting the reason for the change in this file's Sync Impact Report comment.
3. Updating `LAST_AMENDED_DATE` to the amendment date.

Versioning policy:
- **MAJOR**: Removal or redefinition of an existing principle.
- **MINOR**: New principle or section added.
- **PATCH**: Clarification or wording fix with no semantic change.

All implementation plans MUST pass the Constitution Check gate before proceeding.
Complexity violations (e.g., adding a library) MUST be documented in `plan.md` under
Complexity Tracking with a clear justification.

**Version**: 1.0.0 | **Ratified**: 2026-06-05 | **Last Amended**: 2026-06-05
