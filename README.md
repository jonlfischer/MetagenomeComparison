# MetagenomeComparison

This is a simple program used for interactive visualization of metagenomic data. It visualizes a tree of taxonomic assignments of one or more microbial samples.
This program makes use of a space-efficient tree drawing algorithm, the Davidson-Harel-algorithm.
As of now, it loads files from the MEGAN export option "pathDKPCOFGS_to_count" as either single or comparison file.
It can also read a biom-to-tsv exported file created by using qiime2 qiime taxa barplot on a qiime feature table with taxonomic assignments.
When comparing multiple samples, the program builds a "consensus" tree, allowing the user to show single samples or two samples in pairwise comparison, using coloring to display number of reads assigned to the taxa.
Another feature is displaying samples with different timesteps as a timeline which is animated.
Nodes can be selected by clicking and their children shown/hidden by ctrl+clicking.
