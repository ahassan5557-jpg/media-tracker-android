# Week {{N}} Reflection

**Name:**Abdullahi
**Date:**8/1/26

---

## Commits This Week


**Link:**https://github.com/ahassan5557-jpg/media-tracker-android/commit/977649956e4b08e9bbfe4c4e9b7227afe4025b14

---

## Code Review

**Reviewed:** *(pod mate's name)* fasika
**Link to my review:**https://github.com/FasikaYifru/fy-media-tracker-android/pull/12/changes/8a76d9da9a774f285068d78a2a09444496269d14#r3743119844

### What I Looked At

I reviewed the MediaRepository, focusing on how the different functions handle unsuccessful API responses specifically comparing getLibrary(), getLibraryStatus(), getMediaById(), and addToLibrary()
### What I Noticed

I noticed that getLibrary() silently swallows any failed response by returning emptyList(), while every other function in the 
file (getMediaById, getLibraryStatus, addToLibrary) correctly throws an error with a parsed message when the response isn't 
successful. This matters because a failed network call and a genuinely empty library would look identical to whatever screen 
calls getLibrary() there's no way to tell "the user has nothing in their library" apart from "the request just failed."
### Comments I Left
I left a comment suggesting getLibrary() follow the same error-handling pattern as the rest of the file, which was using error() 
with parseErrorMessage on failure, so real failures aren't masked as an empty result.

---

## One Thing I Understood More Deeply

I used to think import errors and "unresolved reference" errors were basically the same kind of problem, but working through the 
asConverterFactory issue that i was facing today taught me they're not a wrong package path (like retrofit2.converter... instead 
of com.jakewharton.retrofit2.converter...) fails silently at the import level, while having two conflicting imports for the same 
function name confuses the compiler in a completely different way. What clicked for me is that Kotlin resolves imports by exact 
package path, not just by function name, so two extension functions can share a name but live in totally different libraries and 
only one of them actually exists in your dependencies.

---

## One Thing I'm Still Confused About

I'm still not fully clear on how to tell in advance whether an icon (or any API) has an "AutoMirrored" variant versus not 
I assumed all deprecated icons had a direct replacement, but StarHalf didn't actually have one, and I don't have a good way 
to check that other than trial and error or asking someone. I'd like to understand what determines which icons get AutoMirrored versions and which don't.

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
