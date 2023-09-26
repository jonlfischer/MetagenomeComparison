package edu.metagenomecomparison.model;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Util {
    public static File[] orderFilesByTime(File[] files){
        if (files.length <= 1)
            return files;
        //from https://stackoverflow.com/questions/17339882/sorting-files-numerically-instead-of-alphabetically-in-java
        class FilenameComparator implements Comparator<File> {
            private static final Pattern NUMBERS =
                    Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            @Override public final int compare(File f1, File f2) {
                String o1 = f1.getName();
                String o2 = f2.getName();
                // Optional "NULLS LAST" semantics:
                if (o1 == null || o2 == null)
                    return o1 == null ? o2 == null ? 0 : -1 : 1;

                // Splitting both input strings by the above patterns
                String[] split1 = NUMBERS.split(o1);
                String[] split2 = NUMBERS.split(o2);
                for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
                    char c1 = split1[i].charAt(0);
                    char c2 = split2[i].charAt(0);
                    int cmp = 0;

                    // If both segments start with a digit, sort them numerically using
                    // BigInteger to stay safe
                    if (c1 >= '0' && c1 <= '9' && c2 >= 0 && c2 <= '9')
                        cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));

                    // If we haven't sorted numerically before, or if numeric sorting yielded
                    // equality (e.g 007 and 7) then sort lexicographically
                    if (cmp == 0)
                        cmp = split1[i].compareTo(split2[i]);

                    // Abort once some prefix has unequal ordering
                    if (cmp != 0)
                        return cmp;
                }

                // If we reach this, then both strings have equally ordered prefixes, but
                // maybe one string is longer than the other (i.e. has more segments)
                return split1.length - split2.length;
            }
        }

        Arrays.sort(files, new FilenameComparator());
        System.out.println(Arrays.toString(files));
        return files;

    }

    //TODO document and this probably does not go here
    public static void collapseBelow(PhyloTree tree, TaxonRank rank){
        ComparativeTreeNode start = ((ComparativeTreeNode) tree.getRoot());
        ((PhyloTree) start.getOwner()).preorderTraversal(start, new Function<Node, Boolean>() {
            @Override
            public Boolean apply(Node node) {
                ComparativeTreeNode cNode = (ComparativeTreeNode) node;

                if (!cNode.isChildrenVisible() && cNode.isVisible())
                    cNode.toggleVisibility();
                boolean goOn = (cNode.getRank().compareTo(rank) <= 0);
                return goOn;
            }
        }, new Consumer<Node>() {
            @Override
            public void accept(Node node) {
                ComparativeTreeNode cNode = (ComparativeTreeNode) node;
                if (cNode.getRank().compareTo(rank) == 0 && cNode.isVisible())
                    cNode.toggleVisibility();
                if (cNode.getRank() == TaxonRank.NORANK) {
                    boolean hasOnlyChildrenWithLowerRank = true;
                    for (Node child : cNode.children()){
                        ComparativeTreeNode cChild = (ComparativeTreeNode) child;
                        if (cChild.getRank().compareTo(rank) <= 0)
                            hasOnlyChildrenWithLowerRank = false;
                    }
                    if (hasOnlyChildrenWithLowerRank && cNode.isVisible())
                        cNode.toggleVisibility();
                }
                }
            });
        }

        public static void uncollapseAll(PhyloTree tree){
            tree.preorderTraversal(tree.getRoot(), new Consumer<jloda.graph.Node>() {
                @Override
                public void accept(jloda.graph.Node node) {
                    ComparativeTreeNode cNode = (ComparativeTreeNode) node;
                    if (!cNode.isChildrenVisible() && cNode.isVisible())
                        cNode.toggleVisibility();
                }
            });
        }



}
