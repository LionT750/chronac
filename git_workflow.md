# Git Workflow Guide

This guide is for first-time contributors. Follow these steps to make changes safely without breaking the main branch.

---

## 1. Clone the repository

```bash
git clone <repository-url>
cd chronac
```

This downloads the project to your computer.

---

## 2. Create a feature branch

Branches are your personal workspace. **Never commit directly to `main`** — it's protected.

```bash
git checkout -b my-feature-name
```

Give your branch a short, descriptive name (e.g. `fix-timeslot-sorting`, `add-csv-parser`).

---

## 3. Make your changes

Edit files, add new ones, or delete what's not needed.

To see what you've changed at any time:

```bash
git status           # which files are modified/added
git diff             # exact lines changed
```

---

## 4. Commit your changes

Think of a commit as a save point with a message explaining *what* you did and *why*.

```bash
git add <file1> <file2>        # stage specific files
git add .                      # stage everything (careful!)
git commit -m "Brief description of the change"
```

**Commit message tips:**
- Use the present tense: "Add CSV parser" not "Added CSV parser"
- Keep it short (< 72 characters)
- If it fixes an issue, reference it: "Fix sorting by date (closes #12)"

---

## 5. Push your branch

```bash
git push origin my-feature-name
```

If this is your first push, Git may ask you to set the upstream:

```bash
git push --set-upstream origin my-feature-name
```

---

## 6. Open a Pull Request (PR)

1. Go to the repository on GitHub
2. You'll see a banner suggesting your recently pushed branch — click **Compare & pull request**
3. Write a short description of what your PR does
4. Click **Create pull request**

Someone will review your code and may request changes.

---

## 7. Address review feedback

If a reviewer asks for changes:

1. Make the edits locally
2. Stage and commit again:

```bash
git add <changed-files>
git commit -m "Address review: fix sorting order"
```

3. Push again (same branch):

```bash
git push origin my-feature-name
```

The PR automatically updates with your new commits.

---

## 8. Merge your PR

Once approved, the reviewer will merge your PR for you (or click **Merge pull request** if you have permission).

---

## 9. Keep your local repo up to date

After your PR is merged, switch back to `main` and pull the latest:

```bash
git checkout main
git pull origin main
```

Delete your old branch (optional):

```bash
git branch -d my-feature-name
```

---

## Common mistakes & how to fix them

| Mistake | Fix |
|---------|-----|
| Committed on `main` by accident | `git checkout -b my-branch` to move those commits to a new branch |
| Forgot to stage a file | `git add <file>` then `git commit --amend` (only if not pushed yet) |
| Wrote a bad commit message | `git commit --amend -m "Better message"` (only if not pushed yet) |
| Need to undo a file change | `git checkout -- <file>` to discard changes since last commit |

---

## Summary

```
git clone <url>
git checkout -b my-feature-name
# make edits
git add <files>
git commit -m "Description"
git push origin my-feature-name
# open PR on GitHub
# wait for review, push more commits if needed
# PR gets merged
git checkout main
git pull origin main
```
