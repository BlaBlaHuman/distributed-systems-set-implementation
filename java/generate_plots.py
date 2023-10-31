import subprocess
import re
import matplotlib.pyplot as plt

THROUGHPUT_RE = re.compile(r"Throughput \(ops/s\):\s+([.0-9E]+)\s+")

def get_throughput(algorithm, threads, size, update_ratio):
    command = ["java", "-cp", "bin", "contention.benchmark.Test",
               "-b", "linkedlists.lockbased.{}".format(algorithm),
               "-d", "2000", "-W", "0"]

    command += ["-t", str(threads)]
    command += ["-i", str(size)]
    command += ["-r", str(size * 2)]
    command += ["-u", str(update_ratio)]

    process = subprocess.run(command, stdout=subprocess.PIPE, encoding="utf8")
    output = process.stdout
    throughput = float(THROUGHPUT_RE.search(output)[1])

    return throughput


ALGORITHMS = ["CoarseGrainedListBasedSet", "HandOverHandListBasedSet", "LazyLinkedListSortedSet"]
THREADS = [1, 4, 6, 8, 10, 12]
SIZES = [100, 1000, 10000]
UPDATE_RATIOS = [0, 10, 100]

def main():
    # Fixed size, fixed update ratio
    fig, ax = plt.subplots()
    for algorithm in ALGORITHMS:
        ys = []
        for threads in THREADS:
            ys.append(get_throughput(algorithm, threads, 1000, 10))
            print(algorithm, ":", threads)
        ax.plot(THREADS, ys, label=algorithm)
    ax.set_title("Fixed size (1000), fixed update ratio (10%)")
    ax.legend()
    plt.savefig("figs/all_out.svg", format="svg")

    # Variable size
    for algorithm in ALGORITHMS:
        fig, ax = plt.subplots()
        for size in SIZES:
            ys = []
            for threads in THREADS:
                ys.append(get_throughput(algorithm, threads, size, 10))
                print(algorithm, ":", size, threads)
            ax.plot(THREADS, ys, label="{} elems".format(size))
        ax.set_title("{}, fixed update ratio (10%)".format(algorithm))
        ax.legend()
        plt.savefig("figs/var_size_{}.svg".format(algorithm), format="svg")

    # Variable update ratio
    for algorithm in ALGORITHMS:
        fig, ax = plt.subplots()
        for update_ratio in UPDATE_RATIOS:
            ys = []
            for threads in THREADS:
                ys.append(get_throughput(algorithm, threads, 100, update_ratio))
                print(algorithm, ":", update_ratio, threads)
            ax.plot(THREADS, ys, label="Upd. ratio {}%".format(update_ratio))
        ax.set_title("{}, fixed size (100 elems)".format(algorithm))
        ax.legend()
        plt.savefig("figs/var_upd_rat_{}.svg".format(algorithm), format="svg")

main()