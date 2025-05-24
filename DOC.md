# Documentation for script ink

## Dialog

## Speak
``{character_name}: {dialog}``

e.g.

``Mark: Hello!``

### Note
You need to spawn it in the world BEFORE making it talk with starting an animation, subscene, cutscene or camera angle where that character is linked.

## Text effect
```
[effect (time=) (force=)]text[/effect]
ex: [waving]hello[/waving]
ex: [shaking time=0.02, force=0.2]text[/effect]
```

## Cutscene

Blocking command: Means that it will continue the story after this command ended.


``cutscene start {cutscene_name}``

## Camera angle

``camera set {parent} {child}``

## Play sound

Name is resource location from minecraft e.g. custom.piano

### Song

``song start {song.name} (volume) (pitch) (loop)``

### Sfx

``sfx start {song.name} (volume) (pitch) (loop)``

## Stop sound

``song stop {song.name}``

``sfx stop {song.name}``

``song stop all``

``sfx stop all``

``sound stop all`` Stop song and sfx at the same time
