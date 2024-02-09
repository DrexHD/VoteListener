# VoteListener

## Setup
This mod is powered by [NuVotifer-Fabric](https://github.com/DrexHD/NuVotifier-Fabric/). Make sure to setup NuVotifier 
first, by following their [Setup Guide](https://github.com/NuVotifier/NuVotifier/wiki/Setup-Guide).

## Config
The config file is located in `./config/votelistener.json`.
```json
{
  // Special characters used in commands need to be escaped. Use a website like 
  // https://www.freeformatter.com/json-escape.html to escape your commands
  
  // Commands have special placeholders:
  // ${uuid} - Vote player UUID
  // ${username} - Vote player name
  // ${serviceName} - Voting website name
  // ${address} - IP address of the voter
  // ${timeStamp} - Timestamp of the vote
  
  // A list of commands that get executed when the vote is received
  "commands": [
    "tellraw @a [{\"text\":\"${username}\",\"color\":\"blue\"},{\"text\":\" voted on \",\"color\":\"aqua\"},{\"text\":\"${serviceName}\",\"color\":\"blue\"}]"
  ],
  // A list of commands that gets executed when the player that voted joins 
  // The commands will be executed by the player, as if they were OP
  // You can use @s to target the player (in commands that support entity selectors)
  "onlineCommands": [
    "give @s diamond 1"
  ]
}
```

## [Placeholders](https://placeholders.pb4.eu/)
- `votelistener:vote_count` - The number of votes a player has accumulated

## [Permissions](https://github.com/lucko/fabric-permissions-api)
You need to be an operator or have these permissions
- `votelistener.reload` - Access to `/votelistener reload` command