/**
 * This fille is part of My Game Projects.
 * Copyright (C) Steve Taylor (sat0198@gmail.com)
 *
 * My Game Prokjects is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * My Game Projects is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.sat0198.games.sudoku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sudoku {
    static final Logger LOGGER = LoggerFactory.getLogger(Sudoku.class);

    public static final int NUM_CELLS = 81;
    public static final int NUM_SETS = 27;
    public static final int NUM_ROWS = 9;

    public static final int ALL_BITS = 0x1ff;

    public static final short[][] SETS = {
            /* Rows */
            { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, { 9, 10, 11, 12, 13, 14, 15, 16, 17 },
            { 18, 19, 20, 21, 22, 23, 24, 25, 26 }, { 27, 28, 29, 30, 31, 32, 33, 34, 35 },
            { 36, 37, 38, 39, 40, 41, 42, 43, 44 }, { 45, 46, 47, 48, 49, 50, 51, 52, 53 },
            { 54, 55, 56, 57, 58, 59, 60, 61, 62 }, { 63, 64, 65, 66, 67, 68, 69, 70, 71 },
            { 72, 73, 74, 75, 76, 77, 78, 79, 80 },
            /* Columns */
            { 0, 9, 18, 27, 36, 45, 54, 63, 72 }, { 1, 10, 19, 28, 37, 46, 55, 64, 73 },
            { 2, 11, 20, 29, 38, 47, 56, 65, 74 }, { 3, 12, 21, 30, 39, 48, 57, 66, 75 },
            { 4, 13, 22, 31, 40, 49, 58, 67, 76 }, { 5, 14, 23, 32, 41, 50, 59, 68, 77 },
            { 6, 15, 24, 33, 42, 51, 60, 69, 78 }, { 7, 16, 25, 34, 43, 52, 61, 70, 79 },
            { 8, 17, 26, 35, 44, 53, 62, 71, 80 },
            /* Squares */
            { 0, 1, 2, 9, 10, 11, 18, 19, 20 }, { 3, 4, 5, 12, 13, 14, 21, 22, 23 },
            { 6, 7, 8, 15, 16, 17, 24, 25, 26 }, { 27, 28, 29, 36, 37, 38, 45, 46, 47 },
            { 30, 31, 32, 39, 40, 41, 48, 49, 50 }, { 33, 34, 35, 42, 43, 44, 51, 52, 53 },
            { 54, 55, 56, 63, 64, 65, 72, 73, 74 }, { 57, 58, 59, 66, 67, 68, 75, 76, 77 },
            { 60, 61, 62, 69, 70, 71, 78, 79, 80 }, };

    public static final short[][] CELL_TO_SETS;

    static {
        CELL_TO_SETS = new short[NUM_CELLS][];

        for (int i = 0; i < NUM_CELLS; ++i) {
            CELL_TO_SETS[i] = new short[3];
        }

        for (int i = 0; i < SETS.length; ++i) {
            final int n = i / NUM_ROWS;
            for (int j = 0; j < SETS[i].length; ++j) {
                CELL_TO_SETS[SETS[i][j]][n] = (short) i;
            }
        }
    }

    static int toBit(int num) {
        for (int i = 0; i < NUM_ROWS - 1; ++i) {
            if (num == 1) {
                return i;
            }
            num >>= 1;
        }
        return NUM_ROWS - 1;
    }

    short bits[];
    short count[];
    int known;

    public Sudoku(String init) {
        this();

        if (init.length() != NUM_CELLS) {
            throw new IllegalArgumentException("Incorrect initialiser string length");
        }

        for (int i = 0; i < NUM_CELLS; ++i) {
            if (init.charAt(i) >= '1' && init.charAt(i) <= '9') {
                set(i, init.charAt(i) - '1');
            }
        }
    }

    public Sudoku() {
        bits = new short[NUM_CELLS];
        count = new short[NUM_CELLS];
        known = 0;

        for (int i = 0; i < bits.length; ++i) {
            bits[i] = ALL_BITS;
            count[i] = NUM_ROWS;
        }
    }

    public void eliminate(int cell, int num) {
        LOGGER.info("Elimination: " + cell + " is " + (num + 1));
        set(cell, num);
    }

    public void lockedPair() {
        for (int i = 0; i < NUM_ROWS; ++i) {
            for (int j = i + 1; j < NUM_ROWS; ++j) {
                final int mask = ~(1 << i | 1 << j);
                for (int k = 0; k < SETS.length; ++k) {
                    int cell = -1;
                    int cell0 = -1;
                    int count = 0;
                    for (int l = 0; l < NUM_ROWS; ++l) {
                        if ((bits[SETS[k][l]] & mask) == 0) {
                            cell0 = cell;
                            cell = SETS[k][l];
                            count++;
                        }
                    }
                    if (count == 2 && this.count[cell] > 1 && this.count[cell0] > 1) {
                        LOGGER.info("LockedPair: " + cell0 + " and " + cell + " have " + (i + 1) + " and " + (j + 1));
                        for (int l = 0; l < NUM_ROWS; ++l) {
                            if (SETS[k][l] != cell0 && SETS[k][l] != cell) {
                                remove(SETS[k][l], i);
                                remove(SETS[k][l], j);
                            }
                        }
                    }
                }
            }
        }
    }

    public void lockedTrio() {
        for (int i = 0; i < NUM_ROWS; ++i) {
            for (int j = i + 1; j < NUM_ROWS; ++j) {
                for (int k = j + 1; k < NUM_ROWS; ++k) {
                    final int mask = ~(1 << i | 1 << j | 1 << k);
                    for (int l = 0; l < SETS.length; ++l) {
                        int cell = -1;
                        int cell0 = -1;
                        int cell1 = -1;
                        int count = 0;
                        for (int m = 0; m < NUM_ROWS; ++m) {
                            if ((bits[SETS[l][m]] & mask) == 0) {
                                cell1 = cell0;
                                cell0 = cell;
                                cell = SETS[l][m];
                                count++;
                            }
                        }
                        if (count == 3) {
                            LOGGER.info("LockedTrio: " + cell1 + " and " + cell0 + " and " + cell + " have " + (i + 1)
                                    + " and " + (j + 1) + " and " + (k + 1));
                            for (int m = 0; m < NUM_ROWS; ++m) {
                                if (SETS[l][m] != cell1 && SETS[l][m] != cell0 && SETS[l][m] != cell) {
                                    remove(SETS[l][m], i);
                                    remove(SETS[l][m], j);
                                    remove(SETS[l][m], k);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void intersect() {
        for (int i = 0; i < NUM_ROWS; ++i) {
            final int bit = 1 << i;
            for (int j = 0; j < SETS.length; ++j) {
                int cell = -1;
                int count = 0;
                for (int k = 0; k < NUM_ROWS; ++k) {
                    if ((bits[SETS[j][k]] & bit) != 0) {
                        cell = SETS[j][k];
                        count++;
                    }
                }
                if (count == 1 && this.count[cell] > 1) {
                    LOGGER.info("Intersection: " + cell + " is " + (i + 1));
                    set(cell, i);
                }
            }
        }
    }

    public void remove(int cell, int num) {
        if (count[cell] > 1) {
            final short bit = (short) (1 << num);

            if ((bits[cell] & bit) != 0) {
                bits[cell] &= ~bit;
                if (count[cell] == 2) {
                    eliminate(cell, toBit(bits[cell]));
                }
                else {
                    --count[cell];
                }
            }
        }
        // else {
        // throw new IllegalArgumentException("Cell is already known");
        // }
    }

    public void set(int cell, int num) {
        if (count[cell] > 1) {
            final short[] sets = CELL_TO_SETS[cell];
            final short bit = (short) (1 << num);

            if ((bits[cell] & bit) == 0) {
                throw new IllegalArgumentException("Cell state conflict");
            }

            for (int i = 0; i < sets.length; ++i) {
                for (int j = 0; j < NUM_ROWS; ++j) {
                    final short other = SETS[sets[i]][j];
                    if (other != cell && count[other] == 1 && bits[other] == bit) {
                        throw new IllegalArgumentException("Cell state conflict");
                    }
                }
            }

            bits[cell] = bit;
            count[cell] = 1;
            known++;

            for (int i = 0; i < sets.length; ++i) {
                for (int j = 0; j < NUM_ROWS; ++j) {
                    final short other = SETS[sets[i]][j];
                    if (other != cell && (bits[other] & bit) != 0) {
                        remove(other, num);
                    }
                }
            }
        }
        else {
            throw new IllegalArgumentException("Cell is already known");
        }
    }

    public void dump() {
        for (int i = 0; i < NUM_ROWS; ++i) {
            for (int j = 0; j < NUM_ROWS; ++j) {
                this.dump(bits[i * NUM_ROWS + j]);
                System.out.print(' ');
                if (j % 3 == 2) {
                    System.out.print(' ');
                }
            }
            System.out.println();
            if (i % 3 == 2) {
                System.out.println();
            }
        }
    }

    public void dump(int bits) {
        for (int i = 0; i < NUM_ROWS; ++i) {
            if ((bits & 1 << i) > 0) {
                System.out.print(i + 1);
            }
            else {
                System.out.print(' ');
            }
        }
    }

    public static void main(String[] args) {
        Sudoku me;
        if (args.length > 0) {
            me = new Sudoku(args[0]);
        }
        else {
            me = new Sudoku();
        }
        me.dump();
        int k;
        do {
            do {
                k = me.known;
                me.intersect();
                me.dump();
            }
            while (k != me.known && me.known < NUM_ROWS * NUM_ROWS);
            me.lockedPair();
            me.dump();
        }
        while (k != me.known && me.known < NUM_ROWS * NUM_ROWS);
        me.lockedTrio();
        me.dump();

        if (me.known == NUM_ROWS * NUM_ROWS) {
            LOGGER.info("Done!");
        }
    }
}
