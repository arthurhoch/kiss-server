# GitHub Pages

## Source

GitHub Pages publishes from `docs/`, following the reference repositories.

## Workflow

`.github/workflows/pages.yml`:

1. checks out the repository;
2. configures Pages;
3. builds `docs/` with Jekyll;
4. uploads the artifact;
5. deploys to GitHub Pages.

## Documentation Rules

- `docs/index.md` is the landing page.
- Markdown files must be readable directly on GitHub.
- Do not require Node or a custom frontend.
- Update docs with public API changes.
- Keep README and docs consistent.
