# Documentation for script ink

## Dialog

## Speak
``<character_name>: <dialog>``

e.g.

``Mark: Hello!``

### Note
You need to spawn it in the world BEFORE making it talk with starting an animation, subscene, cutscene or camera angle where that character is linked.

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

``song start <category.name> [volume] [pitch] [loop=true/false] [fadein fadeTime]``

### Sfx

``sfx start start <category.name> [volume] [pitch] [loop=true/false] [fadein fadeTime]``

## Stop sound

``song stop <category.name> [fadeout fadeTime]``

``sfx stop <category.name> [fadeout fadeTime]``

``song stop all``

``sfx stop all``

``sound stop all`` Stop song and sfx at the same time

## Fade effect

```fade [fadeInValue] [stayValue] [fadeOutValue] [hexColor]```
Shows a color screen