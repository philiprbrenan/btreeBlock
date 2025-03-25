# How to run Vivado synthesis on Azure

## Create an Azure [spot](https://aws.amazon.com/ec2/spot/) instance

Create an Azure [spot](https://aws.amazon.com/ec2/spot/) instance with at least 128GB hard drive and 16GB [memory](https://en.wikipedia.org/wiki/Computer_memory). Update .ssh/config creating a host (perhaps ```aaa```) with the [IP address](https://en.wikipedia.org/wiki/IP_address) address on
Azure with a convenient name to make access easier. Configure [Secure Shell](https://www.ssh.com/ssh) to allow x
forwarding.

Update the Ubunbtu instance:

```
sudo apt update

sudo apt install build-essential tcsh libssl-dev libx11-dev libboost-all-dev \
  libncurses5-dev x11-apps  libxext-dev libxrender-dev \
  libxtst-dev openjdk-21-jdk-headless micro
```

## AMD installer

Download AMD Vivado ```installer``` on the local machine then ```scp``` to the
instance:

```
scp <installer> aaa:   # (note trailing colon on host name)
```

Start the installer on the instance:

```
XINSTALLER_SCALE=2 bash FPGAs_AdaptiveSoCs_Unified_2024.2_1113_1001_Lin64.bin
```

Supply [login](https://en.wikipedia.org/wiki/Login) details for: (https://login.amd.com)


## Clone GitHub

### Update GitHub [Secure Shell](https://www.ssh.com/ssh) [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) Copy ```Azure.pem``` on the local machine to the instance:

```
scp .ssh/Azure.pem aaa:.ssh/
```

On the remote instance extract the public [database key](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) and load it into to GitHub oif
convert the ```pem``` [file](https://en.wikipedia.org/wiki/Computer_file) to r```rsa``` format and load it into GutHub if it
is not already there:

```
ssh-keygen -f Azure.pem -m PEM -p
ssh-keygen -y -f Azure.pem > Azure.pub
```

### Clone the GitHub repo

Clone the [GitHub](https://github.com/philiprbrenan) repo: (https://github.com/philiprbrenan/btreeBlock)

```
GIT_TRACE=1 GIT_CURL_VERBOSE=1  GIT_SSH_COMMAND="ssh -i .ssh/Azure.pem" git clone git@github.com:philiprbrenan/btreeBlock.git
GIT_SSH_COMMAND="ssh -i ~/.ssh/Azure.pem" git pull
```

Install the following if they are not present:

```
sudo cpan install -F Data::Table::text
sudo apt install iverilog
```

# Synthesis

Change to [folder](https://en.wikipedia.org/wiki/File_folder) ```btreeBlock/vivado``` and edit ```synthesis.tcl``` to point
to the [Verilog](https://en.wikipedia.org/wiki/Verilog) [folder](https://en.wikipedia.org/wiki/File_folder) containing the project to be synthesized.

Synthesize the Vivado project:

```
perl synthesis.pl
```
# Enable swap space

This might [help](https://en.wikipedia.org/wiki/Online_help) simulate more [memory](https://en.wikipedia.org/wiki/Computer_memory) if Vovado runs out of [memory](https://en.wikipedia.org/wiki/Computer_memory). Normally it seems to take aboit 15GB.

```
sudo dmesg | grep -i 'oom'
sudo swapon --show
df -h
sudo fallocate -l 128G /swapfile
sudo chmod 600         /swapfile
sudo mkswap            /swapfile
sudo swapon            /swapfile
sudo swapon --show
sudo sysctl vm.swappiness=99
```

# Bash set up

Placing the following commands in your ```.bashrc`` file speeds up development on the command line of a remote host:

```
alias b='cd   ~/btreeBlock/'
alias bv='cd  ~/btreeBlock/vivado'
alias G= "grep -Iinr -P"
alias g='git status; [Git](https://en.wikipedia.org/wiki/Git) commit -m aaa; [Git](https://en.wikipedia.org/wiki/Git) push --force'
alias gg='cd; sudo rm -r ~/btreeBlock/; [Git](https://en.wikipedia.org/wiki/Git) clone git@github.com:philiprbrenan/btreeBlock.git; cd ~/btreeBlock'
alias m='micro'
alias dv='cd  ~/btreeBlock/verilog/delete/vivado/reports'
alias fv='cd  ~/btreeBlock/verilog/find/vivado/reports'
alias pv='cd  ~/btreeBlock/verilog/put/vivado/reports'
alias s='perl ~/btreeBlock/vivado/synthesis.pl'
alias t='top -u azureuser -E g'
alias v='/home/azureuser/Vivado/2024.2/bin/vivado'
alias x='bash ~/btreeBlock/j.sh
```

# Install lxde

On the server install the lightweight desktop lxde

```
sudo apt-get [install](https://en.wikipedia.org/wiki/Installation_(computer_programs)) lxde x2goserver x2goserver-xsession
```

And add a password for ```azureuser``` using:

```
sud
```

On the client:

```
sudo apt-get [install](https://en.wikipedia.org/wiki/Installation_(computer_programs)) x2goclient
```

Then run the client to connect to the graphics desktop provided by the server.

# Using Vivado

Enter ```v``` on the command line of the remote host to run Vivado as a gui .

## Setting font size

```lxappearance``` set font size to 20 and everything should be visible

## IO Pins

Tools->IO Planning-> AutoPlace IO Ports
Then File->Exports->Constraints as xdc file

Set scaling to user defined at 250%
