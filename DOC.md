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

``song start %category.name% [%volume% %pitch% [loop=true/false] [%fadein% fadeTime]]``

### Sfx

``sfx start start %category.name% [%volume% %pitch% [loop=true/false] [%fadein% fadeTime]]``

## Stop sound

``song stop %category.name% [<fadeout> %fadeTime%]``

``sfx stop %category.name% [<fadeout> %fadeTime%]``

``song stop all``

``sfx stop all``

``sound stop all`` Stop song and sfx at the same time

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