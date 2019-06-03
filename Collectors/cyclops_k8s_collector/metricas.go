package main

type MemoryUnit struct {
	Metric  string `json:"metric"`
	Account string `json:"account"`
	Time    int64 `json:"time"`
	Usage   int64 `json:"usage"`
	Unit   string `json:"unit"`
	Data    `json:"data"`
}
type Data struct {
		PodID     string `json:"podId"`
		Container string `json:"container"`
	}