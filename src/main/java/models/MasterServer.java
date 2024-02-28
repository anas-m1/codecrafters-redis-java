package models;

import lombok.Data;

@Data
public class MasterServer extends Server{
    public String replid;
    public int offset;
}
