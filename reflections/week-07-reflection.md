# Week 07 Reflection

**Name:**Abdullahi
**Date:** 7/9/26

---

## Commits This Week

**Link:**https://github.com/ahassan5557-jpg/media-tracker-android/commit/b5b944651457f4eca25c9263d15736ac8e7e3e3a

---

## Code Review


**Reviewed:** *(pod mate's name)* Fasika
**Link to my review:** https://github.com/FasikaYifru/fy-media-tracker-android/pull/8/changes/13af3e00fce0c4ec9dbaec1f5ffc6c2b730a5711#r3686949995

### What I Looked At     click "commits", and copy the URL after filtering by your name or branch. -->


<!-- Walk through the code you reviewed. What was the PR trying to do? Which files or
     functions did you focus on? -->
PR is working on implementing a media detail screen, showing a header with cover image, title, and credi.
I focused on the Column composable in MediaDetailScreen. specifically the stat grid Row and the reviews forEach
loop.
### What I Noticed

I noticed that the stat grid rown and reviws had the most repeated logic and hardcoded data. My review looked into the composable is doing the work of
header, actions, about, stats, reviews and would benefit from being split into smaller named composables for readability.

### Comments I Left

i suggested that it should be split into smaller named composables to help with readability I say this so that the next person or the pr when they return to this work 
can have an easier time understanding and reading the code 

---

## One Thing I Understood More Deeply

today while working it clicked why ?.let {} matters in Compose specifically it's not just null-safety, it controls whether a whole piece of UI even gets built. 
Seeing m.runtimeMinutes?.let { ... } made it click that if the value is null, that stat card just never renders, instead of showing empty or crashing.

---

## One Thing I'm Still Confused About
I'm still not totally clear on where viewModel.mockReviews is supposed to come from in a real app versus this version like, at what point does mock data get swapped
for a real repository call, and how does the ViewModel know to refresh the list after onWriteReview is triggered? I get the concept of a ViewModel holding state, but the lifecycle still feels fuzzy to me

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
