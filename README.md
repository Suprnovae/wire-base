# The Wire Engine

This application takes care of all the plumbing necessary to get the goods
wired from the senders all the way to the recipients.

## Useful Info
The following bits of information may prove very beneficial to developers.

### Console
Get into the application console by running the command ```play```.

Whilst in the console you may start the test/models/UserSpec.java tests by
running ```~ test-only test.models.UserSpec```. I've tried using ```~ test```
but somehow the system freezes after a couple of tests.

I haven't been able to determine if this is a problem with my box, with the
project or with the framework.

### Dump
If you're developing for this project, please remember that
```println("something funny");``` is a true lifesaver. If you ever need to dump
something to the console, don't hesitate to use this method.

### Environment Seperation
I've isolated my Play environment by using a simple bashfile in which I add the
play toolset to my ```PATH``` environment variable.

```
# My engage file
TOOL_BASE="/User/x/path/to/project/path/to/play/play-2.1.3"
PATH=$TOOL_BASE:$PATH
```

The moment I enter my engine directory, I use the ```engage``` bash file that
I load by executing ```source engage```. This loads the environment variables
into the terminal session henceforth allowing the use of the commands offered
as part of the Play toolset (```play``` is the most important command).
Consider my ```engage``` file like a ```.rvmrc``` file that I actively have
execute and not passively by cd-ing into my project directory.

Every version of Play will be added in it's own folder in the
```/User/x/path/to/project/path/to/play``` directory. This will help us to
experiment with different versions of the framework as we go without messing up
our build environments. Just make sure that the version you're referring to in
you makefile is the version you really need.

In my specific case I have everything for the wire in the
```~/Documents/Development/Wire``` directory. All frameworks and tools have
been saved in my ```~/Documents/Development/Wire/tools``` directory.

```
~/Documents/Development/Wire
 |-- tools
 |    +-- play
 |         +-- play-2.1.2
 |         +-- play-2.1.3
 |-- iOS
 |-- Android
 |-- engine
 |    |-- app
 |    |-- conf
 |    |-- logs
 |    |-- project
 |    |-- test
 |    +-- public
 +-- vms
```

Note that the ```engine``` directory is the root of this repository. Meaning
that you really have to determine your own way for structuring the project on
your own dev box.

The directory structure above only gives an indication of a possible structure
that may work. Everyone has a different style, so feel free to tend your home
in accordance to your wishes. Under ```vms``` I have some Virtualbox VM's that
I use to run the PostgreSQL servers that the application uses in test and
development modes.
The mobile app source is contained in the ```iOS``` and ```Android```
directories (pretty self-explanatory). In the engine directory (where I house
the Play project), you see a few directory that are part of a Play project. The
list is not complete but this was just meant to communicate
an idea.
