# Adventures in NEO

This is me working out what the fuck smart contracts on the blockchain look like. Rather than going for everyone's
favourite, Ethereum, I'm playing with Neo.

# Setting Up The Development Environment

Neo is developed in C# and on Windows, I want to play about with writing contracts in Java, and my home machine is an
Ubuntu laptop.  These things do not naturally go together.

After some attempts to get things going on my 17.04 install, including the GUI, I came across the following:
* ["GUI will not be \[supported\]"](https://github.com/neo-project/neo-gui/issues/7)
* ["Ubuntu 17 is not supported"](https://github.com/neo-project/neo-cli)

I probably should have found the second one a little sooner, but never mind.

## A New VM

I initially created a 10GB VM with Ubuntu 16.04, got the CLI compiled and started downloading the blockchain. Then the
hard drive filled up and I realised I'd made two mistakes: small hard drive, and desktop Ubuntu.

1. Install VirtualBox
1. Download [minimal Lubuntu](http://lubuntu.me/downloads/)
1. Install Lubuntu on 25GB HD, 2GB RAM, 64 bit VM
1. Install git
1. Install libs mentioned in [neo-cli readme](https://github.com/neo-project/neo-cli)
1. Snapshot the VM
1. Clone [neo-cli](https://github.com/neo-project/neo-cli)

## Installing .Net

The instructions for compiling the Neo CLI on linux are straightforward: `dotnet build`. However, I don't have the `dotnet`
command.

I'm going to avoid tarballs as much as I can to make life easy and apt-friendly, so I'll follow [the instructions from
Microsoft](https://www.microsoft.com/net/core#linuxubuntu), stopping at the point where it tells me to install the 2.0
SDK. Instead, I'll install the earlier one as described in the readme:

```
sudo apt-get update; sudo apt-get install dotnet-dev-1.1.4
```

Time for `dotnet restore` in the `neo-cli` directory...it starts downloading and about 1G later, it's finished. So I
`dotnet build` and a few seconds later it tells me the build succeeded. So now it's `dotnet neo-cli.dll`.

```
neo>
```

That looks good.

```
neo>show state
Height: 1500/13569, Nodes: 5
```

Excellent! It's syncing a blockchain...but which one?

```
neo>show node
::ffff:106.15.47.180 port:10333 listen:10333 [1/2]
::ffff:5.66.226.193 port:10333 listen:10333 [2/2]
```

Ah, shite! The [testnet config file ](https://github.com/neo-project/neo-cli/blob/master/neo-cli/config.testnet.json)
has different ports. So exit out of the CLI, discover that the Neo team haven't added `Chain/` or `peers.dat` to their
`.gitignore` but I'll assume that those need to be purged.

The docs do mention [changing to the testnet](http://docs.neo.org/en-us/node/testnet.html) and I think what they're
saying is copy the appropriately mentioned files into place. I know next to nothing about .Net development, so I'll just
rebuild the DLL after this.

```
neo>show state
Height: 0/0, Nodes: 5
neo>show node
::ffff... port:20333 listen:20333 [1/5]
...
```

Excellent! That's going to take a while to sync up, so time to get the Java side of things installed. I'll just go with the OpenJDK for now: `sudo apt-get install default-jdk`.

I'll also need to clone the [neo-compiler](https://github.com/neo-project/neo-compiler) and build the Java compiler as
it's required for turning Java class files into a Neo contract. This is another C# project so clone, change into
director, `dotnet restore` and then `dotnet publish`. 

That gives me a DLL, though the docs suggest it should be an exe. No matter, I try running it and it tells me I need a
class file. But first, [this looks interesting](https://github.com/neo-project/neo-devpack-java). Clone, install maven,
attempt to package...and character encoding errors in the source files. Surprisingly, I wrote a command to help me with
identifying character sets some time ago, and tonight it came in handy again. The project uses `x-mswin-936` for its
encoding.

Add the configuration to the pom file, `mvn package` and...I have a jar file! This is clearly going to be needed for
compiling against.

## Writing Some Java

I've been using sbt a little at work, so I'll use it again now. I'll also not bore you with installing Eclipse and
configuring the project.

The Neo devs have been kind enough to give me a [Hello, World](http://docs.neo.org/en-us/sc/getting-started-java.html)
so let's just copy it into place. Perhaps I shouldn't be all that surprised that it doesn't work, especially since the
[Java examples](https://github.com/neo-project/examples-java) repo is empty.

Anyway, it's complaining about not being able to find `FunctionCode` to import, and looking in the dev pack confirms
there isn't one there. There is, however, `SmartContract` so I change it to that, fix the other compiler errors (I think
this might be copied and pasted C#) and the red lines go away.

Drop back to the terminal, move to the Neo compile directory and attempt to pass my new class file to the DLL. Boom! It
can't find `org.neo.smartcontract.framework.jar` in `/usr/share/dotnet/`. Looking at the source for the compiler, that's
a hardcoded filename, so I need to put something there. I don't have a jar by that name, but I do have the one I
compiled the Java against, so...

```
mv neo-devpack-java-2.3.0.jar /usr/share/dotnet/org.neo.smartcontract.framework.jar
```

And try to transpile the class file again...and I have an avm file! Fantastic! My blockchain is still syncing, so I
guess attempting to run it will have to wait for another evening.

# TBD

* Although there's a closed issue saying the GUI is not supported on Linux, there is mention of installing Linux dev libraries in the 
readme, so that needs investigating. In the case that it's not compilable, I'll have to set up a Windows VM and then try to sniff 
network traffic to reverse engineer something for putting contracts on the blockchain.
* Having a self contained executable for the AVM compiler (presumably becoming NVM?) means I can have a better workflow
