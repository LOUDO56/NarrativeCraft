# Documentation for script ink

## Dialog

## Speak
``<character_name>: <dialog>``

e.g.

``Mark: Hello!``

### Note
You need to spawn it in the world BEFORE making it talk with starting an animation, subscene, cutscene or camera angle where that character is linked.
Otherwise, your story will crash and will throw an error at runtime.

## Text effect
```
[<effet> (time=<timeValue>) (force=<forceValue>)]<texte>[/<effet>]
ex: [waving]Hellooo!![/waving]
ex: [shaking time=0.02, force=0.2]I'm angry![/effect]
```

## Cutscene

Blocking command: Means that it will continue the story after this command ended.

``cutscene start <cutscene_name>``

## Camera angle

``camera set <parent> <child>``

## Play sound

Name is resource location from minecraft e.g. custom.piano

### Song

``song start %namespace:category.name% [%volume% %pitch% [loop=true/false] [<fadein> %fadeTime%]]``

### Sfx

``sfx start start %namespace:category.name% [%volume% %pitch% [loop=true/false] [<fadein> %fadeTime%]]``

## Stop sound

``song stop %namespace:category.name% [<fadeout> %fadeTime%]``

``sfx stop %namespace:category.name% [<fadeout> %fadeTime%]``

``song stop all``

``sfx stop all``

``sound stop all`` Stop song and sfx at the same time

### Note about sound

If you don't provide the namespace, minecraft namespace will be used as default namespace.

## Fade effect

``fade [%fadeInValue%] [%stayValue%] [%fadeOutValue%] [%hexColor%]``
Shows a color screen

## Cooldown

``wait %time% <second(s), minute(s), hour(s)>``
Blocking command. Wait before moving on to the next tag or dialog.

## Change chapter or scene

``on enter``
Important and to not remove!! It's used to change between scene and chapter when playing the story.

## Save

``save``
Save on that point

## Start, stop animation or subscene on a scene

Play animation or subscene on the scene linked to it.

``animation start %animation_name%``

``subscene start %subscene_name%``

``animation stop %animation_name%``

``subscene stop %subscene_name%``

## Change day time

Change current day time on the world, you can interpolate between 2 ticks to have a great animation.

``time <set,add> <day,midnight,night,noon,%tick%> [to <day,midnight,night,noon,%tick%> for %time% <second(s), minute(s), hour(s)> [%easing%]]``

## Change weather

Change current weather

``weather set <clear, rain, thunder>``

## Execute minecraft command

Execute a minecraft command, custom or vanilla one.

``command %command_value%``

**IMPORTANT**: Let's say you want to execute that command ``text_display ~ ~ ~ {billboard:"center",text:"hello",background:-65536}``

Ink will throw an error because brackets is used to detect variables, to fix this, you need to put ``\`` before a bracket.

Fixed command: ``text_display ~ ~ ~ \{billboard:"center",text:"hello",background:-65536\}``

## Dialog parameters

Change current parameter of the dialog

``dialog <offset, scale, padding, width, textColor, backgroundColor, gap, letterSpacing, unSkippable, autoSkip> [%value1%] [%value2%]``

Examples:

``dialog offset -1 0.7``

``dialog scale 1.3``

``dialog backgroundColor K862J5``

```
dialog unSkippable // Impossible for the user to skip the dialog.
dialog autoSkip 2 // After the dialog ends, it will autoskip 2 seconds later.
// You can create a cool highlight to a specific dialog that the user must read.
```

## Screen shake

Shake the player screen to emulate an explosion or add more immersion

``shake %strength% %decay_rate% %speed%``

- ``strength`` Shake offset
- ``decay_rate`` Decay rate over time
- ``speed`` Noise frequency

Examples:

```shake 60 5 30```
Will shake fast, like an explosion happened.

```shake 4 0 0.1```
Infinite shake, smooth and slow to add immersion.

```shake 0 0 0```
Reset shake effect