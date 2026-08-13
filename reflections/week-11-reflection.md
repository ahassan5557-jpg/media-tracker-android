# Week 11 Reflection — Bonus Feature Sprint (Week 1 of 2)

*This week's reflection is different from the standard template. We're not doing Profile this week — instead, this is the first of two weeks building your assigned bonus feature (Write Review, Quotes, or Priorities). See `reflection-instructions.md` for naming/submission rules, which are unchanged; only the content below differs.*

**Name:**Abdullahi
**Date:** 8/4/26
**My assigned bonus feature:** quotes

---

## Commits This Week


**Link:** https://github.com/ahassan5557-jpg/media-tracker-android/commit/88cd2be467cd7b26f43686a2f75c7e630786a985

---

## Code Review

<!-- Code review continues as normal — same pod rotation, regardless of which bonus feature you or your pod mate are building. -->

**Reviewed:** *(pod mate's name)* fuchee 
**Link to my review:** https://github.com/fucheeyoung-blip/media-tracker-android/pull/11/changes#r3779209371

### What I Looked At
I reviewed PriorityScreen.kt — specifically the filter chip UI, the loading/error/success state handling, the list rendering with LazyColumn, and the PriorityListItem composable.
### What I Noticed
The priority filter chips have a leadingIcon box meant to show a colored dot per priority level, but the box has no background color set, so it renders as an invisible empty square. I also noticed the errorMessage StateFlow is collected but never displayed to the user
### Comments I Left
I left a comment on the filter chip's leadingIcon box, pointing out that it's missing a .background() call so it currently renders as an invisible square instead of a colored priority indicator, and asked if they could add one using the existing priorityColor() function.
---

## Bonus Feature Progress

<!-- This is the most important section this week. Be concrete: which endpoint(s) did you wire?
     What's actually showing on screen with real data? What's still stubbed or fake?
     "I worked on my bonus feature" is not an answer. "I got POST /quotes working from Media Detail
     and quotes show up in a list on my profile, but I haven't wired edit or delete yet" is. -->

**What's working:**
POST quotes is wired from Media Detail the "Add Quote" button opens a dialog with quote text, optional page number, public/private toggle and saves via MediaDetailViewModel.addQuote() to DefaultMediaRepository.createQuote(). GET quotes is also wired: on load, MediaDetailViewModel fetches the user's quotes and filters them to the current media
**What's still stubbed, fake, or not started:**
Edit, delete, likes/unlikes, the public quotes feed (?public=true), and pagination are all not started  the API service (updateQuote, deleteQuote, likeQuote, unlikeQuote) is declared but unused. There's no dedicated "my quotes" section on the profile screen yet
**What I'm blocked on, if anything:**
how to get the quotes to my profile 

---

## One Thing I Understood More Deeply


--- Building addQuote() in MediaDetailViewModel made the difference between optimistic and non-optimistic UI updates click for me. With addToLibrary()/toggleFavorite(), the state flips instantly and the network call happens in the background because there's nothing to wait for. But with quotes, I can't update the list until the POST actually succeeds, because the server assigns the id and I need that before the quote can be edited, deleted, or liked later.

## One Thing I'm Still Confused About

I'm not fully sure how to handle the GET /quotes failure case. Right now it swallows errors into an empty list (same pattern as getReviews()), so a real network failure and "you just have no quotes" look identical to the user
---

## Anything Else *(optional)*

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Concrete progress report (what's wired, what's not) plus specific, honest "Understood More Deeply" and "Still Confused" sections. | Present but vague — "I worked on my feature" with no specifics on what's actually working. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** I check that the review actually exists on GitHub before grading. The written summary here and the GitHub comment should match.
