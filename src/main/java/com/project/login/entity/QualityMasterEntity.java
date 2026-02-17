package com.project.login.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qualitymaster")
public class QualityMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private String width;

    @Column
    private String reed;

    @Column
    private String pick;

    @Column
    private String warp;

    @Column
    private String weft;

    @Column(name = "reed_space")
    private String reedSpace;

    @Column
    private String weave;

    @Column(name = "qulity_name")
    private String qualityName;

    @Column
    private String alias;

    // ===== GETTERS & SETTERS =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getWidth() { return width; }
    public void setWidth(String width) { this.width = width; }

    public String getReed() { return reed; }
    public void setReed(String reed) { this.reed = reed; }

    public String getPick() { return pick; }
    public void setPick(String pick) { this.pick = pick; }

    public String getWarp() { return warp; }
    public void setWarp(String warp) { this.warp = warp; }

    public String getWeft() { return weft; }
    public void setWeft(String weft) { this.weft = weft; }

    public String getReedSpace() { return reedSpace; }
    public void setReedSpace(String reedSpace) { this.reedSpace = reedSpace; }

    public String getWeave() { return weave; }
    public void setWeave(String weave) { this.weave = weave; }

    public String getQualityName() { return qualityName; }
    public void setQualityName(String qualityName) { this.qualityName = qualityName; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
}
