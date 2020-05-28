# CauldronBrewing
Allows you to brew potions in a modded cauldron. To create the cauldron, simply put a 
vanilla cauldron in a crafting bench, with blaze powder underneath it. All recipes are
the same as in Vanilla. 

### Planned Features

- Integration with JEI is on the roadmap.
- Apotheosis integration was planned, but on hold until further notice.
- Optional "Potion Sickness" and related debuffs for when you have too many potion effects. 
- Higher tier cauldrons (For mixing potions?)
- Potion mixing
- Putting potions back into the cauldron. 
- Jumping into the cauldron gives you the effect, at the cost of the liquid in the cauldron. 
- Breaking the cauldron may leave a lingering cloud behind. 
 


### Datapacks
You can now add potions and recipes through datapacks in vanilla. All you need are the
the folders `potions` and `brewing`. 
You declare potions in the `potions` folder, and brewing recipes in `brewing`. Here are 
the formats:

```
potion.json
{
  "name": "modid:resource",
  "effects": [
  {
    "amplifier": 0,  # 0-255
    "duration": 3600 # duration
    "effect": "modid:effect_name"
  },
  ... more effects ...
  ]
}
```
An amplifier of 0 results in a potion that is level 1. Duration is in multiples of 3 seconds (?).
If the effect is unknown, the recipe will just be ignored. 
```
brewing.json
{
  "type": "cauldron:brewing",
  "base": {
    "potion_type": "modid:resource", #usually "cauldron:awkward"
    "amount" : 500   #0-1000 (default 500)
  } ,
  "reagent": {
    "item": "modid:item",
    "tag": "modid:tag"   # one or the other, never both
  },
  "result": {
    "potion_type": "modid:resource" # the name defined in potion.json
    "amount" : 500 #0-1000 (default 500)
  }
}
```
Amount corresponds to millibuckets always. The reagent can be any item registerd, or any tag registered. Base and result default to "cauldron:awkward" and EMPTY respectively. Only the reagent is required, everythign else has defaults. 
