# powergrid-outages-detection
Detecting outages in power system using Machine Learning: k-Means clustering and k-NN classification

# Clustering samples using kMean
1. The samples have been imported from the database and stored in the internal memory as an ArrayList of objects. In the ArrayList, the samples have been sorted out by time (ascending, from time=1 to time=200). Every object in the ‘Samples’ class has its time and values defined. The values are the actual voltages and angles, stored as a List (first voltages for bus 1 ... 9 (ascending), then angles in the same order).
2. In order to get the right centroids (best-fit), the following steps have been conducted:
    1) Four centroids have been initialized using random samples.
    2) For every sample, the Euclidean distances between the sample and all centroids have been
    calculated, the centroid with the smallest distance is assigned to the sample.
    3) Now that the samples have been clustered, new centroids have been calculated for every cluster. New centroids have been calculated as a mean of all samples that belong to particular clusters (simply calculating mean value over every dimension and thus getting all the new-
    centroids’ dimensions).
    4) Distance between old centroids and their corresponding new centroids have been calculated.
    Of these distances, the biggest is stored as ‘maximal distance’.
    5) The centroids are reassigned to the new ones.
    6) Steps 1) to 6) are being repeated (in a while loop), until the maximal distance is smaller than
    the threshold (defined as a very small value).
    7) When the while loop has been executed, the average distance between all samples and
    centroids assigned to them is calculated.
    8) The above steps are repeated 1000 times and the centroids with the smallest average distance
    are regarded as best-fit centroids (this to make sure we end up having every centroid in
    different cluster).
3. Having identified the best-fit centroids, step 2) is conducted to actually cluster the samples.
4. The above steps are conducted for the Learning set and for the Test set.

# Clustering samples using kNN
1. For a query sample in the Test set, the distances to all other samples (already clustered using kMeans) are calculated and stored in an ArrayList of samples (called ‘resultList’), in which every sample has its cluster defined and its distance to the query sample.
2. This ArrayList is then sorted out by distance, ascending. This is done by using a comparator.
3. K-nearest samples are picked up from the ArrayList (k number is user-defined using try-and-error method, however for this assignment it doesn’t matter that much as the clusters are located quite
far away from each other and it is easy to distinguish between them).
4. The chosen k-nearest samples are being counted regarding their clusters. The cluster with highest
frequency is assigned as the cluster for the query sample.
5. The above steps are being repeated until all samples from the Test set are clustered.
6. A test function is used, which tests if the kNN clustering algorithm has clustered all the samples in
the same way as the kMeans algorithm (used for the Test set). The algorithm compares the cluster number assigned by kMean with the cluster number assigned by kNN and counts the samples that match.
In order to see a cluster of a particular sample (of Learn set or Test set), f.ex. time=6, one can access the sample as the 6th object in the ArrayList containing clustered samples. On the other hand, GUI provides users with graphical visualisation of samples on a voltage-angle diagrams.

# Naming the clusters
To identify the different states of the system using the clusters, some conditions were tested on the clusters detected. Each cluster name required to test different conditions to identify its corresponding cluster.
After having the correct centroids of the four clusters thanks to the kNN or kMeans methods, the centroid values corresponding to the voltages of every bus in the system were added and compared between the four clusters. This procedure gave as result the assignment of two names:
- High load rate during peak hours: which was the cluster with the lowest sum of voltages in the buses of the system.
- Low load rate during night: which was the cluster with the highest sum of voltages in the buses.
To prove when a generator was being disconnected, the differences in voltages between generator buses and the adjacent buses were calculated. Instead of using each sample, the centroids were used again for simplicity. The test works since the generator buses does not have any load connected to them, therefore, if the generator is out then there is no current between this and the adjacent bus, causing a zero-voltage drop. Therefore, the minimum voltage difference between a generator bus to its adjacent bus was taken for each cluster and compared with each other to find the minimum voltage difference. The minimum voltage difference cluster was defined as:
- Shut down of generator for maintenance.
Finally, the last name was assigned to the last cluster since there are no more options. This name is
- Disconnection of a line for maintenance