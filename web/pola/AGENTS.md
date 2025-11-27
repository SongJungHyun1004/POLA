# Repository Guidelines

## Project Structure & Module Organization
- `src/app`: App Router routes (e.g., `home`, `timeline`, `my`), shared UI under `app/components`, global styles in `globals.css`, and the root layout in `layout.tsx`.
- `src/api`: Fetch helpers (`apiClient`, `serverApiClient`) centralize base URLs, headers, and error handling for browser/server contexts.
- `src/services`: Domain logic for files, categories, auth, sharing, and RAG search; components should call these functions instead of accessing APIs directly.
- `src/dtos`: Request/response DTOs shared between services and UI, ensuring strong typing.
- `src/store`: Zustand stores such as `useAuthStore` for cross-route state.
- `public`: Static assets (icons, images) served as-is; generated output lives in `.next` and must stay untracked.

## Build, Test, and Development Commands
- `yarn install`: Install dependencies with the pinned Yarn 4.1.0 manager defined in `package.json`.
- `yarn dev`: Launch the Next.js dev server at `http://localhost:3000` with hot reloading.
- `yarn lint`: Run ESLint (`eslint.config.mjs`) to enforce Next.js + TypeScript rules; this must pass before committing.
- `yarn build`: Create the optimized production bundle and catch type/runtime issues early.
- `yarn start`: Serve the contents of `.next` for production-mode smoke tests.

## Coding Style & Naming Conventions
TypeScript runs in `strict` mode with the `@/*` alias from `tsconfig.json`; favor importing from aliases over relative paths. Use two-space indentation, semicolons, and arrow functions for callbacks. Name components and layouts in PascalCase (`TimelineCard`), hooks with a `use` prefix, and services with meaningful verbs or `*Service`. Keep Tailwind-first utility classes in JSX (`bg-[#FFFEF8]`), share tokens via `globals.css`, and scope feature styles close to their routes. Prefer small, focused components inside `src/app/components` and keep data fetching inside `src/services`.

## Testing Guidelines
An automated test runner is not configured yet, so every change must at least pass `yarn lint`, `yarn build`, and manual verification in `yarn dev`. When adding coverage, follow the standard Next.js stack (React Testing Library + Jest/Vitest) and store files next to the implementation using the `*.test.ts(x)` suffix. Mock network access at the `apiClient` layer rather than touching `fetch` directly. Prioritize tests for business-critical flows such as file uploads, category CRUD, and auth-guarded pages; document any new fixtures or test data in the PR.

## Commit & Pull Request Guidelines
Mirror the existing Git history by prefixing commits with intent tags like `Feat`, `Fix`, or `Design` followed by a concise summary (`Feat: 카테고리 CRUD 페이지 추가`). Each PR should include: purpose and scope, affected routes/services, linked issues, screenshots or clips for UI alterations, and a checklist confirming `yarn lint`/`yarn build` ran locally. Mention new environment variables or migrations explicitly and keep PRs focused—open follow-ups for unrelated work.

## Environment & Security
Secrets belong in `.env.local`; never commit them. Use `serverApiClient` for requests that require server-only tokens, and avoid exposing credentials through client components. Review new dependencies before adding them, prefer incremental changes, and ensure static assets placed in `public` comply with licensing requirements.
