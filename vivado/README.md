# How to run Vivado synthesis on Azure

## Create an Azure spot instance

Create an Azure spot instance with at least 128GB hard drive and 16GB memory.

Update .ssh/config creating a host (perhaps ```aaa```) with the ip address on
Azure with a convenient name to make access easier. Configure ssh to allow x
forwarding.

Update the Ubunbtu instance:

```
sudo apt update
sudo apt install build-essential tcsh libssl-dev libx11-dev libboost-all-dev \
  libncurses5-dev x11-apps  libxext-dev libxrender-dev \
  libxtst-dev openjdk-21-jdk-headless​ micro
``

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

Supply login details for: ```https://login.amd.com```


## Clone GitHub

### Update GitHub ssh key

Copy ```Azure.pem``` on the local machine to the instance:

```
scp .ssh/Azure.pem aaa:.ssh/
```

On the remote instance extract the public key and load it into to GitHub oif
convert the ```pem``` file to r```rsa``` format and load it into GutHub if it
is not already there:

```
ssh-keygen -f Azure.pem -m PEM -p
ssh-keygen -y -f Azure.pem > Azure.pub
```

### Clone the GitHub repo

```
GIT_TRACE=1 GIT_CURL_VERBOSE=1  GIT_SSH_COMMAND="ssh -i .ssh/Azure.pem" git clone git@github.com:philiprbrenan/btreeBlock.git
​GIT_SSH_COMMAND="ssh -i ~/.ssh/Azure.pem" git pull
```

# Synthesis

Change to folder ```btreeBlock/vivado``` and edit ```synthesis.tcl``` to point
to the verilog folder containing the project to be synthesised.

Synthesise the Vivado project:

```
perl synthesis.pl
```
