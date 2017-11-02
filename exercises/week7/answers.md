Group: 23

*Name 1:* Patrick Evers Bjoerkman (pebj@itu.dk)
*Name 2:* Davide Laezza (dlae@itu.dk)
*Name 3:* Richard Banyi (riba@itu.dk)


# Exercise 7.1
## Task 1-3

*Implement concurrent download. You can ignore the cancellation button and progress bar for now. There
seems to be two ways to implement concurrent download of N webpages. Either (1) create N SwingWorker
subclass instances that each downloads a single webpage; or (2) create a single SwingWorker subclass
instance that itself uses Java’s executor framework to download the N web pages concurrently. Approach
(1) seems more elegant because it uses the SwingWorker executor framework only, instead of using two
executor frameworks. Also, approach (2) seems dubious unless it is clear that a SwingWorker’s publish
method can be safely called on multiple threads; what does the Java class library documentation say about
this? Implement and explain the correctness of your solution for concurrent download.

According to the Java class library documentation, publish sends data chunks to process and according to the source
code of JDK8, accumulativeRunnable is used which defines that process should be called (it is this runnable that will be picked
up by the UI thread). AccumulativeRunnable has a synchronized method called add which accumulates the argument from calls to publish.
This makes publish safe to call from multiple threads. 
I don't use publish/process in my solution. I create N SwingWorker subclasses which each downloads a single webpage. If cancelled or complete they
will exit the doInBackground method with the result. The UI thread will then call the done method of the respective SwingWorker instance which will
append the result to the textArea and call setProgress. This setProgress needs to be propagated to the outer SwingWorker (DownloadWorker) which is done by
adding an addPropertyChangeListener to each SwingWorker on the progress property (this will then recall setProgress to forward the result to the listeners of DownloadWorker'safe
addPropertyChangeListener). DownloadWorker Will await the completion of all SwingWorker subclasses or for a cancellation by synchronizing on an atomicInteger called count. This will
be incremented by each SwingWorker upon download completion of a webpage. Using notifyAll inside SwingWorker will then notify the outer DownloadWorker which calls wait to check if the count variable is equals
the number of SwingWorker subclasses created (or urls processed) or if cancellation has been thrown. If that is the case, exit doInBackground and write "done" to the textArea in the done method.
Ultimately this will schedule N threads as a new thread will be created for each SwingWorker instance. This way we will have concurrent download of all webpages written out in any order depending of which completes first.
