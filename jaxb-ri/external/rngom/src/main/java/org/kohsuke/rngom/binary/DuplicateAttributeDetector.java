package org.kohsuke.rngom.binary;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.rngom.nc.NameClass;

class DuplicateAttributeDetector {
    private List nameClasses = new ArrayList();
    private Alternative alternatives = null;

    private static class Alternative {
        private int startIndex;
        private int endIndex;
        private Alternative parent;

        private Alternative(int startIndex, Alternative parent) {
            this.startIndex = startIndex;
            this.endIndex = startIndex;
            this.parent = parent;
        }
    }

    boolean addAttribute(NameClass nc) {
        int lim = nameClasses.size();
        for (Alternative a = alternatives; a != null; a = a.parent) {
            for (int i = a.endIndex; i < lim; i++)
                if (nc.hasOverlapWith((NameClass) nameClasses.get(i)))
                    return false;
            lim = a.startIndex;
        }
        for (int i = 0; i < lim; i++)
            if (nc.hasOverlapWith((NameClass) nameClasses.get(i)))
                return false;
        nameClasses.add(nc);
        return true;
    }

    void startChoice() {
        alternatives = new Alternative(nameClasses.size(), alternatives);
    }

    void alternative() {
        alternatives.endIndex = nameClasses.size();
    }

    void endChoice() {
        alternatives = alternatives.parent;
    }

}
