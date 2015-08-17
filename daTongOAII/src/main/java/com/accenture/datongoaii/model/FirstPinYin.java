package com.accenture.datongoaii.model;

import java.util.ArrayList;
import java.util.List;

public class FirstPinYin {
	public String mFirstPinYin;

	FirstPinYin() {
	}

	FirstPinYin(String firstPinYin) {
		mFirstPinYin = firstPinYin;
	}

	public static List<?> createPinYinGroupedList(List<?> list) {
		List<List<Object>> indexedArray = new ArrayList<List<Object>>(27);
		for (int i = 0; i < 28; i++) {
			indexedArray.add(new ArrayList<Object>());
		}

		for (Object obj : list) {
			FirstPinYin pinYin = (FirstPinYin) obj;
			char ch = pinYin.mFirstPinYin.charAt(0);
			if (ch >= 'A' && ch <= 'Z') {
				indexedArray.get(ch - 'A' + 2).add(pinYin);
			} else {
				if (obj instanceof Dept) {
					indexedArray.get(0).add(pinYin);
				} else if (obj instanceof Group) {
					indexedArray.get(1).add(pinYin);
				}
			}
		}

		for (int i = 27; i >= 0; i--) {
			List<Object> tmpList = indexedArray.get(i);
			if (tmpList.size() == 0) {
				indexedArray.remove(i);
			}
		}
		return indexedArray;
	}

}
