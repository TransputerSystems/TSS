package uk.co.transputersystems.occam.metadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ed on 31/03/2016.
 */
public class Workspace {

    private List<WorkspaceLocation> locations = new ArrayList<>();

    private int size = 0;
    private int numTemporaries = 0;
    private int maxNumTemporaries = 0;
    private int expansionAreaSize;
    private int maxExpansionAreaSize;
    private boolean functionSpace;

    private int idGenerator = 0;

    private int id;
    private transient Workspace parent;

    public Workspace(Workspace parent, boolean functionSpace) {
        this.parent = parent;
        this.expansionAreaSize = 5;
        this.maxExpansionAreaSize = this.expansionAreaSize;
        this.functionSpace = functionSpace;

        if (parent != null) {
            this.id = parent.generateWorkspaceId();

            allocateVariable(new Variable("[PARENT WPTR]", Integer.MIN_VALUE, "POINTER"));
        } else {
            this.id = idGenerator++;
        }
    }

    public int getId() {
        return id;
    }
    public int getInitSize() {
        int result = size;

        if (functionSpace) {
            int args = 0;
            for (WorkspaceLocation location : locations) {
                if (location.itemIndex < 0) {
                    args++;
                }
            }
            args = Math.max(args, 3);
            result -= args;

            result--; // Return Ptr
        }

        return result;
    }
    public int getSize() {
        return size;
    }
    public int getFullSize() {
        return size + maxExpansionAreaSize;
    }
    public int getExpansionAreaSize() {
        return expansionAreaSize;
    }
    public int getMaxTempsSize() {
        return maxNumTemporaries;
    }
    public Workspace getParent() {
        return parent;
    }

    public void allocateTemporary() {
        locations.add(new WorkspaceLocation(0));
        numTemporaries++;
        if (numTemporaries > maxNumTemporaries) {
            maxNumTemporaries = numTemporaries;
        }
    }

    public void allocateVariable(Variable variable) {
        //TODO: Handle variables > one word in size
        //TODO: Handle the word size using some form of config file
        locations.add(new WorkspaceLocation(0, variable.getIndex()));
    }

    public void allocateArgument(Argument argument) {
        //TODO: Handle arguments > one word in size
        //TODO: Handle the word size using some form of config file
        locations.add(new WorkspaceLocation(0, -argument.getIndex()));
    }

    public void removeTemporaryFromWorkspace() {
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).itemIndex == Integer.MAX_VALUE) {
                locations.remove(i);
                break;
            }
        }
        numTemporaries--;
        if (numTemporaries < 0) {
            throw new IndexOutOfBoundsException("Cannot remove more temporaries than were added!");
        }
    }

    public void updateSizeAndOffsets() {
        // Order by item index (descending)
        locations.sort((WorkspaceLocation wsl1, WorkspaceLocation wsl2) -> Integer.compare(wsl2.itemIndex, wsl1.itemIndex));

        // Reserve WPtr+0
        size = 1;

        // Add space for temporaries
        size += maxNumTemporaries;

        int lastItemIndex = Integer.MAX_VALUE;
        for (WorkspaceLocation wsl : locations) {

            // Ignore temporaries
            if (wsl.itemIndex != Integer.MAX_VALUE) {

                // Gone from above zero to below zero
                if (wsl.itemIndex < 0 && lastItemIndex >= 0) {
                    // Hit args or parent WS pointer
                    break;
                }

                //TODO: Deal with locals/args which are bigger than one word
                wsl.offset = size++;
            }

            lastItemIndex = wsl.itemIndex;
        }

        if (functionSpace) {
            // Add one to offset for return pointer
            size++;

            // Order by item index (ascending)
            locations.sort((WorkspaceLocation wsl1, WorkspaceLocation wsl2) -> Integer.compare(wsl1.itemIndex, wsl2.itemIndex));

            int numArgs = 0;
            WorkspaceLocation parentWSPtrWSL = null;
            for (WorkspaceLocation wsl : locations) {
                if (wsl.itemIndex >= 0) {
                    break;
                }

                if (wsl.itemIndex == Integer.MIN_VALUE) {
                    parentWSPtrWSL = wsl;
                }

                numArgs++;
            }

            for (int i = numArgs; i < 3; i++) {
                size++;
            }

            if (parentWSPtrWSL != null) {
                parentWSPtrWSL.offset = size++;
            }

            size += numArgs - (parentWSPtrWSL != null ? 1 : 0);

            int offset = size - 1;
            for (WorkspaceLocation wsl : locations) {
                if (wsl.itemIndex >= 0) {
                    break;
                }

                if (wsl != parentWSPtrWSL) {
                    wsl.offset = offset--;
                }
            }
        } else {
            for (WorkspaceLocation wsl : locations) {
                if (wsl.itemIndex >= 0) {
                    break;
                }

                if (wsl.itemIndex == Integer.MIN_VALUE) {
                    wsl.offset = size++;
                }
            }
        }
    }

    public int getLastTemporaryOffset(int distance) {
        return (maxNumTemporaries - numTemporaries + 1 + distance); // Add one for WPtr+0 then add one to get from count value to index value
    }
    public int getNextTemporaryOffset() {
        return (maxNumTemporaries - (numTemporaries+1)) + 1; // Add one for reserved location: WPtr+0
    }
    public int getOffset(int itemIndex) {
        for (WorkspaceLocation wsl : locations) {
            if (wsl.itemIndex == itemIndex) {
                return wsl.offset;
            }
        }
        return -1;
    }
    public int getNthTemporaryOffset(int n) {
        return  (maxNumTemporaries - n) + 1;
    }

    public Workspace getOwner(int itemIndex) {
        for (WorkspaceLocation wsl : locations) {
            if (wsl.itemIndex == itemIndex) {
                return this;
            }
        }

        if (parent != null) {
            return parent.getOwner(itemIndex);
        }

        return null;
    }

    private int generateWorkspaceId() {
        if (parent != null) {
            return parent.generateWorkspaceId();
        } else {
            return idGenerator++;
        }
    }

    public void growExpansionArea(int amount) {
        expansionAreaSize += amount;
        if (expansionAreaSize > maxExpansionAreaSize) {
            maxExpansionAreaSize = expansionAreaSize;
        }
    }
    public void shrinkExpansionArea(int amount) {
        if (amount > expansionAreaSize) {
            throw new IllegalArgumentException("Shrink amount cannot be larger than expansion area size!");
        }
        expansionAreaSize -= amount;
    }
}
