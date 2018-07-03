package helpers

import "sort"

func BinarySearch(data []int, search int) int {
	i := sort.Search(len(data), func(i int) bool { return data[i] >= search })
	if i < len(data) && data[i] == search {
		return i
	}

	return -1
}
