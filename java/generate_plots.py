import subprocess
import re
import matplotlib.pyplot as plt
import numpy as np

THROUGHPUT_RE = re.compile(r"Throughput \(ops/s\):\s+([.0-9]+)\s+")

def get_throughput(algorithm, threads, size, update_ratio):
    command = ["java", "-cp", "bin", "contention.benchmark.Test",
               "-b", "linkedlists.lockbased.{}".format(algorithm),
               "-d", "2000", "-W", "0"]

    command += ["-t", str(threads)]
    command += ["-i", str(size)]
    command += ["-r", str(size * 2)]
    command += ["-u", str(update_ratio)]

    process = subprocess.run(command, capture_output=True, text=True, encoding="utf8")
    output = process.stdout
    throughput = float(THROUGHPUT_RE.search(output)[1])

    return throughput


ALGORITHMS = ["CoarseGrainedListBasedSet", "HandOverHandListBasedSet", "LazyLinkedListSortedSet"]
THREADS = [1, 4, 6, 8, 10, 12]
SIZES = [100, 1000, 10000]

def main():
    fig, ax = plt.subplots()

    # Fixed size, fixed update ratio
    for algorithm in ALGORITHMS:
        throughputs = []
        for threads in THREADS:
            throughputs.append(get_throughput(algorithm, threads, 1000, 10))
            print(algorithm, ":", threads)
        ax.plot(THREADS, throughputs, label=algorithm)
    ax.set_title("Fixed size (1000), fixed update ratio (10%)")
    ax.legend()
    plt.savefig("figs/all_out.svg", format="svg")


main()
# print(get_throughput("CoarseGrainedListBasedSet", 10, 1024, 10))