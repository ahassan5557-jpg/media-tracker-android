# Week 12 Reflection — Bonus Feature Sprint (Week 2 of 2, Final)

*Second and last week of bonus feature work. Week 13 has no build time — this is the last chance to get your feature demo-ready before Week 14. This template replaces the standard weekly reflection, same as last week.*

**Name:** Abdullahi
**Date:** 8/13/26
**My assigned bonus feature:** Quotes

---

## Commits This Week

**Link:** https://github.com/ahassan5557-jpg/media-tracker-android/commit/3fe49fe5b6154369cdabe8c2d08d425cbf02a362

---

## Code Review

**Reviewed:** *(pod mate's name)* fuchee
**Link to my review:** https://github.com/fucheeyoung-blip/media-tracker-android/pull/12/changes#r3788045423

### What I Looked At
I reviewed my fuchee's DefaultPriorityRepository, specifically the updatePriorities() function that sends updated priority items to the backend.
### What I Noticed
Their own comment states that PUT /priorities replaces the whole list since there's no per-item endpoint, 
but the implementation contradicts that, it calls api.updatePriorities(writeItem) inside a for loop, 
sending one item at a time instead of the full list in a single request. If the endpoint really does a full replace, 
this would mean each loop iteration overwrites the previous one, leaving only the last item surviving by the time the loop finishes.
### Comments I Left
I left a comment pointing out the contradiction between their comment and the code, and asked whether api.updatePriorities() should instead accept
the entire priorities list and be called once, rather than looping and calling it per item.

---

## Bonus Feature — Final Status


**What works end-to-end, right now:**
right now a user can add a quote from a book's Media Detail screen with text, optional page number, public/private toggle, 
see it appear immediately in the quotes list on that same screen, and see it again under "My Quotes" on their Profile this
is confirmed working live, not just compiling. They can edit or delete their own quotes (delete has a confirm dialog first), 
and both update the list without a full reload. On the separate Public Quotes screen, a user can browse public quotes from all users, 
like and unlike them with the heart icon updating the count , and load additional pages via a "Load More" button once they reach the end of the current page.
**Tests written for this feature:**
One unit test (MediaDetailViewModelQuoteTest) verifies that calling addQuote() on the ViewModel correctly appends the new quote to uiState's quote list, 
using a fully mocked DefaultMediaRepository and SessionRepository
**Known gaps or rough edges going into demos:**
I haven't tested the "Load More" pagination button against a real dataset large enough to actually trigger a second page 
the logic is in place , but I haven't visually confirmed a second page appending. Also i havent had time to really play with the test and make them to my liking its not smooth i got it to work once but i am not so sure about it 
---

## One Thing I Understood More Deeply

I used to think "the feature isn't working" meant something was wrong with my code specifically. This week taught me that a feature can be built completely correctly 
and still fail end-to-end because of a bug in a totally different, unrelated file  what i saw was my Quotes feature was fine the whole time, but I couldn't verify it 
because SearchResultsViewModel was silently returning fake data that never matched real backend IDs. What clicked was learning to actually isolate where a failure is happening 
i used Logcat to see the raw network calls instead of assuming the newest code I wrote is automatically the problem.

---

## One Thing I'm Still Confused About
I'm still confused about my MediaDetailViewModelQuoteTest and if it actually satisfies the spec's testing requirement, so right now it verifies addQuote() updates the state correctly with a mocked repository,
but I don't know if that alone counts as "enough" or if edit/delete/like also needed their own tests. I also had a lot of trouble just getting the test file itself to run at all tonight it kept landing in the
wrong source folder and throwing unresolved reference errors, so I'm not 100% sure it's actually passing smoothly versus just finally compiling. I'd like confirmation on whether one passing test covering addQuote() 
is sufficient, or if I should add more before this counts as done
---

## Anything Else *(optional)*

<!-- Anything about the bonus feature sprint as a whole — the two-week format, being assigned a
     feature rather than choosing it, whatever's on your mind — is fair game here. -->

---

## Rubric

*You don't need to self-assess — this is here so you know what I'm looking at.*

| Section | Points | Full Credit | Half Credit | No Credit |
|:---|:---:|:---|:---|:---|
| **Reflection** | 10 | Honest final-status report — what works end-to-end, what's rough, what's tested — plus a specific, genuine "Understood More Deeply" that reflects on the sprint as a whole, not just this week. | Present but vague, or only reports on this week rather than the feature's overall state. | Missing or one-word answers. |
| **Code Review** | 10 | Specific observation about the code with explanation of why it matters (or a substantive positive comment). Link to review present and verified. | A question or comment that shows you read the code, but lacks explanation. | "Looks good!" or equivalent. Missing link. Review not found on GitHub. |
| **Total** | **20** | | | |

**A note on the code review score:** same as every other week — I check the link before grading.
