# Deploying Ridex (Render + Vercel)

This deploys your backend to **Render** (free tier, Docker-based) and your
frontend to **Vercel** (free tier). Both pull directly from your GitHub
repo, so once set up, pushing to `main` redeploys automatically.

Total time: ~20-30 minutes the first time.

---

## Part A — Backend on Render

### A1. Create a free PostgreSQL database

1. Go to https://dashboard.render.com and sign up/log in (you can use your
   GitHub account).
2. Click **New +** → **PostgreSQL**.
3. Name it `ridex-db`, choose the **Free** plan, pick a region close to you.
4. Click **Create Database**. Wait ~1 minute for it to spin up.
5. On the database's page, scroll to **Connections** and note down:
   - **Internal Database URL** (looks like `postgresql://user:pass@host/dbname`)
   - You'll convert this to a JDBC URL in step A3.

### A2. Create the web service

1. Click **New +** → **Web Service**.
2. Connect your GitHub account if you haven't, then select your `ridex` repo.
3. Render will detect the `Dockerfile`. Set:
   - **Name**: `ridex-backend`
   - **Root Directory**: `backend`
   - **Environment**: `Docker` (should auto-detect from the Dockerfile)
   - **Plan**: Free
4. Don't click "Create" yet — first add the environment variables below.

### A3. Set environment variables

Still on the create screen (or under the service's **Environment** tab if
you already created it), add:

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | `jdbc:postgresql://<host>/<dbname>` — take the host and dbname from your Internal Database URL (step A1), and prefix with `jdbc:postgresql://` instead of `postgresql://` |
| `DATABASE_USERNAME` | the username from the Internal Database URL |
| `DATABASE_PASSWORD` | the password from the Internal Database URL |
| `FRONTEND_ORIGIN` | leave blank for now — you'll fill this in after deploying the frontend (Part B) |

Click **Create Web Service**. Render will build your Docker image (takes
3-5 minutes the first time) and deploy it.

### A4. Get your backend URL

Once deployed, Render shows a URL like:

```
https://ridex-backend.onrender.com
```

Test it by visiting `https://ridex-backend.onrender.com/api/users` — you
should see `[]`.

**Note**: Render's free tier sleeps after 15 minutes of inactivity. The
first request after sleeping takes 30-60 seconds to wake up — this is
normal for free hosting and worth mentioning if you link this from your
resume.

---

## Part B — Frontend on Vercel

### B1. Import the project

1. Go to https://vercel.com and sign up/log in with GitHub.
2. Click **Add New** → **Project**.
3. Select your `ridex` repo.
4. Set **Root Directory** to `frontend`.
5. Vercel auto-detects Vite — framework preset should show "Vite".

### B2. Set the environment variable

Before deploying, add an environment variable:

| Key | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://ridex-backend.onrender.com/api` (your Render URL from A4, with `/api` on the end) |

Click **Deploy**. Takes about a minute.

### B3. Get your frontend URL

Vercel gives you a URL like:

```
https://ridex-yourname.vercel.app
```

This is your **live demo link** for your resume.

---

## Part C — Connect them (CORS)

Your backend needs to allow requests from your new Vercel URL.

1. Go back to Render → your `ridex-backend` service → **Environment**.
2. Set `FRONTEND_ORIGIN` to your Vercel URL, e.g.
   `https://ridex-yourname.vercel.app` (no trailing slash).
3. Save — Render will redeploy automatically (~1 minute).

---

## Part D — Verify

1. Open your Vercel URL.
2. Try creating a rider, a driver, and requesting a ride.
3. If you see a "backend not connected" type error, double check:
   - The Render service is awake (visit the backend URL directly first to
     wake it up, since free tier sleeps)
   - `VITE_API_BASE_URL` on Vercel matches your Render URL exactly
   - `FRONTEND_ORIGIN` on Render matches your Vercel URL exactly (no
     trailing slash, correct `https://`)

---

## Updating after changes

Both platforms auto-redeploy when you push to `main`:

```
git add .
git commit -m "describe your change"
git push
```

Render rebuilds the Docker image; Vercel rebuilds the frontend. No manual
redeploy needed.

---

## For your resume

A good format:

```
Ridex — Ride-Hailing Platform (Spring Boot, React, PostgreSQL)
Live demo: https://ridex-yourname.vercel.app
Source: https://github.com/deepagarwal-tech/ridex
```

Worth mentioning in a project description: custom Dijkstra-based routing
engine, REST API design, full-stack deployment (Docker on Render +
Vercel), persistent PostgreSQL database.
