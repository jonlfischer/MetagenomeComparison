package edu.metagenomecomparison.model;

import java.util.HashMap;

public enum TaxonRank implements Comparable<TaxonRank>{
    NORANK,
    ROOT,
    DOMAIN,
    KINGDOM,
    PHYLUM,
    CLASS,
    ORDER,
    FAMILY,
    GENUS,
    SPECIES;

    public static HashMap<String, TaxonRank> taxonRankMap(){
        HashMap<String, TaxonRank> map = new HashMap<>();
        map.put("d", DOMAIN);
        map.put("k", KINGDOM);
        map.put("p", PHYLUM);
        map.put("c", CLASS);
        map.put("o", ORDER);
        map.put("f", FAMILY);
        map.put("g", GENUS);
        map.put("s", SPECIES);
        map.put("0", NORANK);
        return map;
    }

    public static HashMap<TaxonRank, String> taxonRankMapRev(){
        HashMap<TaxonRank, String> map = new HashMap<>();
        map.put(DOMAIN, "d");
        map.put(KINGDOM, "k");
        map.put(PHYLUM, "p");
        map.put(CLASS, "c");
        map.put(ORDER, "o");
        map.put(FAMILY, "f");
        map.put(GENUS, "g");
        map.put(SPECIES, "s");
        map.put(NORANK, "0");
        map.put(ROOT, "0");
        return map;
    }

    public static HashMap<TaxonRank, Integer> taxonRankToFontSize(){
        HashMap<TaxonRank, Integer> map = new HashMap<>();
        map.put(ROOT, 20);
        map.put(DOMAIN, 20);
        map.put(KINGDOM, 18);
        map.put(PHYLUM, 16);
        map.put(CLASS, 14);
        map.put(ORDER, 12);
        map.put(FAMILY, 10);
        map.put(GENUS, 8);
        map.put(SPECIES, 8);
        map.put(NORANK, null);
        return map;
    }
}

