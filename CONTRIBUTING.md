# Contributing

Thanks for considering a contribution to `spring-openclaw-integration`.

## Before You Start

- Please open an issue first for significant changes so we can align on scope.
- Keep pull requests focused. Small and reviewable changes get merged faster.
- Never commit real tokens, secrets, private URLs, or production data.

## Development Environment

- JDK 17
- Maven 3.9+ recommended
- A reachable OpenClaw gateway if you want to run the integration end to end

## Local Setup

1. Configure `src/main/resources/application.yml` with local test values.
2. Start the application with `mvn spring-boot:run`.
3. Run tests with `mvn test`.

## Coding Guidelines

- Follow the existing Spring Boot project structure.
- Prefer clear names and small focused classes or methods.
- Add or update tests when changing behavior.
- Keep public-facing documentation in sync with code changes.

## Pull Request Checklist

- The change is explained clearly in the PR description.
- Tests were added or updated when behavior changed.
- Documentation was updated if setup, config, or API behavior changed.
- No secrets or local-only config values were committed.

## Commit Messages

Use short, descriptive commit messages. Conventional Commits are welcome but not required.

## Questions

If something is unclear, open an issue and describe the use case or blocker.
