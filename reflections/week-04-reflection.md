# Week 4 Reflection

**Name:** Abdullahi Hassan
**Date:** 6/11/26

---

## Commits This Week

**Link:** https://github.com/ahassan5557-jpg/media-tracker-android/pull/4

---

## Code Review


**Reviewed:** Fasika
**Link to my review:** https://github.com/FasikaYifru/fy-media-tracker-android/pull/5/changes/a0bae17ddc4fbb3bd001e028098c44cb41d13adc#r3400608063

### What I Looked At
I reviewed the signupviewmodel handling user registration specifically the onsignupclicked() function and the input change handeling. their goal was to implement signup validation. making sure blank fields and to match passwords. I review on that fucntions control flow

### What I Noticed
I noticed that inside the onsignupclicked() the else branch of when block calls viewmodelscope.launch again, even thought the entire block is already running inside the outer viewmodelscope.launch. that brings about unnecessayr redundency. the inner launch is not needed. and it could cause problems because it the outter is cancelled the inner could keep running independently.
### Comments I Left
I left a comment talking about the redundant usage in the else branch, I did say it seemed redundant because I am second guessing myself maybe the PR has a reason for it, but i did point it out to bring it to their attention 
---

## One Thing I Understood More Deeply
i understood and learned today about keyboard actions, what clicked was imeaction controls which buttons shows up on the keyboard such as next done and search button replacing the the default enter key and keyboard actions defines 
what code actually runs when the user taps on that button. the prof talked about it and then i asked my pod mate and they really explained it alot better

---

## One Thing I'm Still Confused About
im confused about why im getting an error in my registerscreen, it has a problem with my by statement (lines 60-64, saying that it has no method, I dont where i went wrong, I dont know how to fix it, ive been trying for 2hrs now,
maybe i need to get some sleep. there is also a collectasstate error, which i dont know why.  
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
