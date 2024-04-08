# Drain-java


## Introduction

drain-java focuses on inference mode, loading and using template status information from a saved snapshot. When a new log message comes in, Drain-Java compares it with an existing template to identify and match similar tokens

This project was originally written in Java version by referring to the [Drain project](https://github.com/logpai/Drain3).

Read more information about Drain from the following paper:
- Pinjia He, Jieming Zhu, Zibin Zheng, and Michael R. Lyu. [Drain: An Online Log Parsing Approach with Fixed Depth Tree](https://jiemingzhu.github.io/pub/pjhe_icws2017.pdf), Proceedings of the 24th International Conference on Web Services (ICWS), 2017.
 
## Usage

Java 8 is required to run drain-java.

**Example:**

Input HDFS_2k Log ([Loghub-2.0](https://github.com/logpai/loghub-2.0)):

```
081109 203615 148 INFO dfs.DataNode$PacketResponder: PacketResponder 1 for block blk_38865049064139660 terminating
081109 205931 13 INFO dfs.DataBlockScanner: Verification succeeded for blk_-4980916519894289629
```

Program Arguments:

```
Usage: java -jar xx.jar [-hv] [--verbose] [-it] [-l=LOG_FORMAT_STRING] [-f=SNAPSHOT_BIN_PATH] [-r=REDIS_PATH] [-k=KAFKA TOPIC PATH] FILE
```

Currently Available Arguments: 

```
Usage: java -jar xx.jar [-hv] [-i] [-f=SNAPSHOT_BIN_PATH] FILE"
```

Drain-Java(Inference Mode) loads snapshot(bin file):

```
----------------------------
1: ID=1     : size=311       : PacketResponder <*> for block blk <*> terminating
2: ID=2     : size=314       : BLOCK* NameSystem.addStoredBlock: blockMap updated: <*> is added to blk <*> size <*>
3: ID=3     : size=292       : Received block blk <*> of size <*> from <*>
4: ID=4     : size=292       : Receiving block blk <*> src: <*> dest: <*>
5: ID=5     : size=115       : BLOCK* NameSystem.allocateBlock: <*> temporary/ task <*> <*> <*> <*> <*> blk <*>
6: ID=6     : size=20        : Verification succeeded for blk <*>
7: ID=7     : size=263       : Deleting block blk <*> file <*> <*>
8: ID=8     : size=80        : <*> Served block blk <*> to <*>
9: ID=9     : size=80        : <*> exception while serving blk <*> to <*>
10: ID=10    : size=224       : BLOCK* NameSystem.delete: blk <*> is added to invalidSet of <*>
11: ID=11    : size=1         : 10.250.15.198:50010 Starting thread to transfer block blk 4292382298896622412 to 10.250.15.240:50010
12: ID=12    : size=2         : BLOCK* ask <*> to delete blk <*>
13: ID=13    : size=2         : Received block blk <*> src: <*> dest: <*> of size 67108864
14: ID=14    : size=2         : BLOCK* ask <*> to delete blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*> blk <*>
15: ID=15    : size=1         : BLOCK* ask 10.250.14.38:50010 to replicate blk -7571492020523929240 to datanode(s) 10.251.122.38:50010
16: ID=16    : size=1         : BLOCK* ask 10.251.126.5:50010 to delete blk -9016567407076718172 blk -8695715290502978219 blk -7168328752988473716 blk -4355192005224403537 blk -3757501769775889193 blk -154600013573668394 blk 167132135416677587 blk 2654596473569751784 blk 5202581916713319258
----------------------------
```

Some Program output (Matching):

```
----------------------------
Section 'match' took 0.00150380 seconds (Total: 0.00150380 seconds, CallCount: 1, Average Time Per Call: 0.00150380 seconds)
# INPUT: PacketResponder 1 for block blk_38865049064139660 terminating
# OUTPUT: PacketResponder <*> for block blk <*> terminating
----------------------------
Section 'match' took 0.00007120 seconds (Total: 0.00265210 seconds, CallCount: 13, Average Time Per Call: 0.00020401 seconds)
# INPUT: Receiving block blk_1724757848743533110 src: /10.251.111.130:49851 dest: /10.251.111.130:50010
# OUTPUT: Receiving block blk <*> src: <*> dest: <*>
----------------------------
```


### Extract Snapshot bin file

Refer to the applicable [Drain project](https://github.com/logpai/Drain3)

```
import json
import os
import sys
import time

from drain3 import TemplateMiner
from drain3.template_miner_config import TemplateMinerConfig
from drain3.file_persistence import FilePersistence

# 스냅샷 파일 경로 설정
snapshot_file_path = "/content/HDFS_2k_snapshot_without.bin"

# 파일 기반 Persistence Handler 구성
persistence_handler = FilePersistence(snapshot_file_path)

# drain3 파라미터 설정 로드
config = TemplateMinerConfig()
config.load("drain3.ini")
config.profiling_enabled = True

if not os.path.exists(snapshot_file_path):
    print("스냅샷 파일 존재 X")

# TemplateMiner 인스턴스 생성 시 Persistence Handler 전달
template_miner = TemplateMiner(persistence_handler, config=config)

# 로그 파일 처리 함수
def process_log_file(file_path):
    line_count = 0
    with open(file_path) as f:
        lines = f.readlines()

    for line in lines:
        line = line.rstrip()
        line = line.partition(": ")[2]
        result = template_miner.add_log_message(line)
        line_count += 1
        if result["change_type"] != "none":
            result_json = json.dumps(result)
            # print(f"Input ({line_count}): {line}")
            # print(f"Result: {result_json}")

# 첫 번째 로그 파일(event.log) 처리
print("Processing event.log...")
process_log_file("HDFS_2k.log")

sorted_clusters = sorted(template_miner.drain.clusters, key=lambda it: it.size, reverse=True)
for cluster in sorted_clusters:
    print(cluster)

template_miner.profiler.report(0)

# 스냅샷 저장
template_miner.save_state(snapshot_reason="HDFS_2k.log")
```
