package me.togaparty.notable_opencv.utils;

import java.util.List;

class NthElement {

    static void nthElementImpl2(List<Double> list, int beg, int end) {
        for(int i = beg + 1; i < end; i++) {

            for(int j = i; j > beg; j--) {
                if(list.get(j - 1) < list.get(j)) {
                    break;
                }
                double f = list.get(j);
                list.set(j, list.get(j - 1));
                list.set(j - 1, f);
            }
        }
    }

    static void nthElementImpl1(List<Double> list, int beg, int end, int index) {
        if(beg + 4 >= end) {
            nthElementImpl2(list, beg, end);
            return;
        }
        int initial_beg = beg;
        int initial_end = end;

        // Pick a pivot (using the median of 3 technique)
        double pivA = list.get(beg);
        double pivB = list.get((beg + end) / 2);
        double pivC = list.get(end - 1);
        double pivot;

        if(pivA < pivB) {
            if(pivB < pivC)
                pivot = pivB;
            else pivot = Math.max(pivA, pivC);
        } else {
            if(pivA < pivC)
                pivot = pivA;
            else pivot = Math.max(pivB, pivC);
        }

        // Divide the values about the pivot
        while(true) {

            while(beg + 1 < end && list.get(beg) < pivot){
                beg++;
            }
            while(end > beg + 1 && list.get(end - 1) > pivot){
                end--;
            }
            if(beg + 1 >= end) {
                break;
            }
            // Swap values
            double f = list.get(beg);
            list.set(beg, list.get(end - 1));
            list.set(beg, f);

            beg++;
            end--;
        }
        if(list.get(beg) < pivot) {
            beg++;
        }
        if(beg == initial_beg || end == initial_end) {
            throw new RuntimeException("No progress. Bad pivot");
        }
        if(index < beg) {
            nthElementImpl1(list, initial_beg, beg, index);
        } else {
            nthElementImpl1(list, beg, initial_end, index);
        }
    }

    static double nthElement(List<Double> list, int index) {
        nthElementImpl1(list, 0, list.size(), index);
        return list.get(index);
    }
}
