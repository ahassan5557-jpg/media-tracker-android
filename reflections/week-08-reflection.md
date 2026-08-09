# Week {{N}} Reflection

**Name:**Abdullahi
**Date:**8/1/26

---

## Commits This Week

**Link:**https://github.com/ahassan5557-jpg/media-tracker-android/commit/a7cb20b9596a766eb12f3c4714204f45264eaaca

---

## Code Review

**Reviewed:** Fasika 
**Link to my review:**https://github.com/FasikaYifru/fy-media-tracker-android/pull/10/changes/ed82fecf1740cf1dc38b3ea0d77a07e1fb05f6c8#r3742737279

### What I Looked At
I reviewed my pod mate's implementation of the media detail screen's state handling, specifically the "when" 
block that covers Loading, Error, and Success states for uiState,

### What I Noticed
I noticed that the Error state only displays state.message in a Text composable with no way for the user to recover, 
if the media fails to load, they're stuck on that screen with no retry or back option, 
other than force-closing the app or using the phone's back button.

### Comments I Left
I left a suggestion to add a "Retry" or "Go Back" button below the error message, 
so a temporary failure (bad connection, server hiccup) doesn't leave the user at a dead end with no path forward.

---

## One Thing I Understood More Deeply

I used to think the fake data in FakeMediaRepository was just separate hardcoded lists, but I realized objects like ActivityEvent 
actually reference shared objects like user = userJordan, media = mediaList[4]) instead of duplicating that data every time. That 
clicked for me because I used to just copy-paste values instead of reusing a reference, and now I get why that matters. if Jordan's 
follower count changes, it updates everywhere Jordan is referenced instead of needing to be changed in five places.

---

## One Thing I'm Still Confused About

I'm not clear on the transition plan from FakeMediaRepository to a real API — does this object get deleted once we wire up real network calls, 
or does it stay as a fallback/testing tool? I also don't know if "repository" here matches the actual Repository pattern or if this is just 
temporary scaffolding meant to be replaced entirely.

---

## Anything Else *(optional)*

<!-- Did you help a pod mate work through something? Did you discover something cool or frustrating?
     Did something from a previous week finally click? This is a good place to put it. -->

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Specific, honest responses to "More Deeply" and "Still Confused" sections. Shows genuine thinking — not just "I learned X." | Responses are present but vague or generic ("I got better at Compose"). | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match. If the review isn't there, the written summary can't earn credit.
