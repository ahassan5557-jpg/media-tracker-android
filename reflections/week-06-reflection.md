# Week 06 Reflection

**Name:** Abdullahi 
**Date:**7/2/26

---

## Commits This Week

**Link:** https://github.com/ahassan5557-jpg/media-tracker-android/pull/6/commits

---

## Code Review



**Reviewed:** *(pod mate's name)*fuchee
**Link to my review:**https://github.com/fucheeyoung-blip/media-tracker-android/pull/7/changes/a2898231848beb1bd7e07e92654bac25a453a13b#r3517031816

### What I Looked At

the pr is trying to implement a login function inside the repository  that implements AuthRepository. I focused on overridesuspendfunlogin method. They are trying to handle user authentication

### What I Noticed

i noticed an error that could cause problems becuse the method response()!! will throw a NullPointerException if the server returns a 200 with an empty body,
this is important because that  will cause it to crash instead you could have it returning an error state by having a fallback to LoginResult.UnknownError.

### Comments I Left
i left a suggestion to have it return to an error state and how he could do that, I did that because i think right now his code could be crashing 

---

## One Thing I Understood More Deeply

i am starting to get a much better feel for the login screen and register screen and how they connect it just seems that there isnt as much fog, like i know my way around it, because ive spent so much time
i think its given me a better understanding

---

## One Thing I'm Still Confused About
im still confused about the client id that you sent us and where we are supposed to be using it and how we should implement it 

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
