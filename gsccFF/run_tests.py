import subprocess
import tempfile
import sys, getopt
from threading import Timer

# Options & problems
options = {
    "only-gs": ["gs"],
    "gs-random": ["gs", "rand"],
    "gs-rpgascending": ["gs", "rpg", "asc"],
    "gs-rpgdescending": ["gs", "rpg", "desc"],
    "no-gs": []
}

problems = {
    "rovers": [[str(i)] for i in range(1, 21)],
    "satellite": [[str(i)] for i in range(1, 21)],
    "driverlog": [[str(i)] for i in range(1, 21)],
    "elevators": [[str(i), str(j)] for i in range(1, 31) for j in range(5)],
    "freecell": [[str(i)] for i in range(1, 21)]
}

additional_problems = {
    "frecell-addiitonal": [[str(i), str(j)] for i in range(2, 14) for j in range(1, 6)]
}

# Run gradle task
def run_task(task_name, timeout=300):
    

    with tempfile.TemporaryFile() as tempout, tempfile.TemporaryFile() as temperr:
        proc = subprocess.Popen("gradle --no-daemon {}".format(task_name), stdout=tempout, stderr=temperr, shell=True)
        
        # Timer
        timer = Timer(timeout, lambda process: process.kill(), [proc])

        try:
            timer.start()
            proc.wait()
        finally:
            timer.cancel()
            proc.kill()

        # If there's an error, write error to file
        if proc.returncode > 0:
            with open("test_errors/{}.txt".format(task_name), 'w') as error:
                temperr.seek(0)
                error.write(temperr.read().decode("utf-8"))
                return (proc.returncode, None)
        tempout.seek(0)
        return (proc.returncode, tempout.read().decode("utf-8"))

def run_domain(domain, params, problem):
    parameter = "-"
    if len(params) > 0:
        for param in params:
            parameter += "{}-".format(param)
        
    task_name = "{}{}{}".format(domain, parameter, '-'.join(problem))
    
    returncode, out = run_task(task_name)

    if returncode > 0:
        # Response,planLength,totalTime,ehcTime,bfsTime
        return (False, -1, -1.0 , -1.0, -1.0)
    
    out_split = out.split('\r\n')
    plan_length = int(out_split[-9].split(' ')[-1])
    total_time = float(out_split[-8].split(' ')[-1].split('s')[0])
    ehc_time = float(out_split[-7].split(' ')[-1].split('s')[0])
    bfs_time = float(out_split[-6].split(' ')[-1].split('s')[0])

    return (True, plan_length, total_time, ehc_time, bfs_time)


def test_domain(domain, option):
    problems_in_domain = problems[domain]
    params = options[option]
    results = {}
    
    print("Running on {} with {}: ".format(domain, "-".join(params)))
    for problem in problems_in_domain:
        problem_label = "-".join(problem)
        print("Problem {}...".format(problem_label))
        results[problem_label] = run_domain(domain, params, problem)
        print("Done.")
        
    with open("test_results/{}/{}-{}.csv".format(domain, domain, option), 'w') as file:
        file.write("problem,planLength,totalTime,ehcTime,bfsTime\n")
        for result in results:
            res = results[result]
            if res[0] == False:
                file.write("{},-,-,-,-\n".format(result))
            else:
                file.write("{},{},{},{},{}\n".format(result, res[1], res[2], res[3], res[4]))


def test_all_options(domain):
    for option in options:
        test_domain(domain, option)



def test_on(domains):
    for domain in domains:
        test_all_options(domain)

def test_on_options(domains):
    for domain in domains:
        for option in domains[domain]:
            test_domain(domain, option)


domains = {
    "driverlog": ["no-gs"],
    # "freecell": ["only-gs", "gs-rand", "gs-rpgascending", "gs-rpgdescending", "no-gs"]
} 

test_on_options(domains)