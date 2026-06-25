# Week 05 Reflection

**Name:** Abdullahi
**Date:**6/18/26

---

## Commits This Week

**Link:** https://github.com/ahassan5557-jpg/media-tracker-android/pull/5/commits

---

## Code Review


**Reviewed:** *(pod mate's name)* fuchee
**Link to my review:** https://github.com/fucheeyoung-blip/media-tracker-android/pull/6/changes/0cb6bf8fdaffb6442500786fd796f28ce034ecd3#r3440167783

### What I Looked At

i foucused on AuthViewModel.kt  more specifically on viewModelScope.launch the pr was trying to handle the differnt possible outcomes for the register result if there was a conflict network error or unkown error. this matters because the user needs to know 
what the problem is, is it a network error? or was the username already taken?

### What I Noticed

I think they have the skeleton correct so far as in the have in place the framework for error handeling but all three error branches map to the same generic string 
resource (error_empty_credentials) regardless of whether it was a conflict, network failure, or unknown error meaning users get no actionable feedback about what actually
went wrong. This matters because a user hitting a username already taken conflict needs to know to try a different username, not just that their credentials are empty. 

### Comments I Left

i pointed it out but knowing we are in early stages i think my group mate just wanted to set up the skeletons 

---

## One Thing I Understood More Deeply

i finnaly understand why viewmodels handles validation. i didnt know which class was responsible and now i undestand why viewmodel needs to do it instead of the ui 
or another class doing it, becuause ui is best for displaying things 

---

## One Thing I'm Still Confused About

i still dont understand why we have a defultUserRepository and a UserRepository it seems like they are two seperete things that do the same thing, why not combine them to a single class?

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
