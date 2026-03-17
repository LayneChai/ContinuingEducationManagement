# Local Startup

## Quick start

- Run `start-all.bat` to open backend and frontend in two terminal windows.
- Or run them separately:
  - `start-backend.bat`
  - `start-frontend.bat`

## Default addresses

- Frontend: `http://127.0.0.1:5173`
- Backend: `http://127.0.0.1:8080`
- API docs: `http://127.0.0.1:8080/doc.html`

## Notes

- Backend script uses bundled JDK 17 from `.tools/jdk-17.0.18+8`.
- Default admin account:
  - username: `admin`
  - password: `Admin@123456`
- If `DEEPSEEK_API_KEY` is not set, AI chat page still opens, but returns a config reminder instead of real model output.

## Recommended AI setup

Before starting the backend, set the key in the current terminal session:

```bat
set DEEPSEEK_API_KEY=your_deepseek_key
start-backend.bat
```

## Optional environment variables

- `DEEPSEEK_API_KEY`
- `DEEPSEEK_BASE_URL`
- `DEEPSEEK_MODEL`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_DATABASE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
