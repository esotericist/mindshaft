# mindshaft

a density-focused minimap, loosely inspired by Rei's Minimap of the days of yore.

has: fullscreen toggle, configurable zoom levels, an ugly player cursor, a fairly esoteric indicator of whether voids are above or below player, and a small amount of stutter to rendering that will probably drive some people spare

todo: threaded processing (lol someday), more configurable performance facets (meantime, you can just drop problematic zoom levels), rendering settings (not everyone will want the fancy colored bits?), cheat mode? (disable light checks)

meantime: it works. ship it.

### okay but seriously what is it?

a spatial-only minimap (no mobs, no waypoints, just surrounding area) that attempts to convey notions of "density" to indicate the presence or absence of blocks

### but why?

as it happens, i (esotericist) suffer from some spatial reasoning issues that make it difficult for me to navigate underground. in particular, minecraft's caves are extremely easy for me to get lost in, sometimes resulting in me going in circles.

### but how does this odd thing do anything for that problem?

by providing a geospatial representation that can give hints in a semi-3-dimensional fashion.

take this span of geography:
![2019-10-08_02 38 48](https://user-images.githubusercontent.com/1569754/66385129-d6c19600-e974-11e9-9ed5-4ab1e41bbb0b.png)

a vanilla map will represent this space as so:
![image](https://user-images.githubusercontent.com/1569754/66385374-3d46b400-e975-11e9-96e6-1e72b02b1e47.png)

some other map mod might attempt to provide additional shading or contour information, but they're still essentially restricted to showing you what's on the surface with minimal information about shape.

a minimap might provide a cave mode, but those are typically simply the surface map in slices, providing little information in the way of connectivity.

enter mindshaft:
![image](https://user-images.githubusercontent.com/1569754/66384992-92ce9100-e974-11e9-8747-fdbf0f078938.png)

### but what do all of these colors even mean?

all three color channels provide key information.

green: apparent presence of a solid block. absence of green, absence of block. caveat: unlit areas are treated as denser than lit areas; if you have a completely unlit cave, it won't show up as a void, it will show as solid ground. similarly, the void beneath bedrock is considered 'solid'.

blue: absence of blocks above the player. this represents sky, or it could be considered as a 'blue shift', voids closer to ('moving towards') the 'camera'.

red: absence of blocks below the player. this represents the danger of falling, or it could be considered as a 'red shift', voids further from the 'camera'.

the combination of these colors provides a great deal of information.

transparent blocks, such as tree leaves, will have less green than a solid block, but unlike air blocks, transparent blocks will never have red or blue tinting, as there is still a block in that location.

coincidentally, this typically results in rivers and beaches showing lots of blue while you're standing on the shore: the water blocks are not solid (so are not green), but are not air, so there is in those spaces. Usually, rivers and beaches are under open sky, so there is quite a lot of blue from the air above.

chasms will often show as magenta, due to the air above (blue) and the air below (red) combining.

where this notation really shines, however, are cave systems:

![image](https://user-images.githubusercontent.com/1569754/66386290-d3c7a500-e976-11e9-8328-2c98418e7994.png)

there is a clear gradient of blue -> magenta -> red indicating a passage which is leading downwards.

naturally, the information only goes so far, as typically caves aren't lit except at the surface, or where there happens to be lava.

Another cave system:
![2019-10-08_02 56 04](https://user-images.githubusercontent.com/1569754/66386533-4173d100-e977-11e9-88f8-7b5aba03444b.png)

![2019-10-08_02 57 24](https://user-images.githubusercontent.com/1569754/66386738-8435a900-e977-11e9-8d53-b63238f59597.png)

![2019-10-08_02 57 35](https://user-images.githubusercontent.com/1569754/66386760-8a2b8a00-e977-11e9-810a-01d8e8f5949f.png)

![2019-10-08_02 57 57](https://user-images.githubusercontent.com/1569754/66386766-8dbf1100-e977-11e9-9103-466394b542c3.png)

