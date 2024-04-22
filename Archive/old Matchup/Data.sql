CREATE TABLE Fighters (
    FighterName VARCHAR(64) PRIMARY KEY,
    Types VARCHAR(64),
    BasePower Int,
    StrType VARCHAR(64),
    StrVal Int,
    WkType VARCHAR(64),
    WkVal Int
)

CREATE TABLE Teams (
    TeamName VARCHAR(64) PRIMARY KEY,
    GamesPlayed Int,
    GamesWon Int
)


