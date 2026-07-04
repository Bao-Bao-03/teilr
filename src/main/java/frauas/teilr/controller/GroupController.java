package frauas.teilr.controller;

import frauas.teilr.entity.Group;
import frauas.teilr.entity.User;
import frauas.teilr.service.GroupService;
import frauas.teilr.service.GroupViewService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class  GroupController {
    private final GroupService groupService;
    private final GroupViewService groupViewService;

    @GetMapping("/new")
    public String newGroupForm() {
        return "fragments/group-form :: groupFormContent";
    }
    @PostMapping
    public String createGroup(@RequestParam String name,
                              @RequestParam(required = false) List<Long>memberIds,
                              HttpSession session,
                              Model model) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) return "redirect:/auth/login";

        List<Long> members = (memberIds != null) ? memberIds : List.of();
        groupService.createGroup(name, adminId, members);

        model.addAttribute("groups", groupService.getGroupsForUser(adminId));
        return "fragments/group-list :: groupListContent";
    }

    @GetMapping
    public String listGroupForUser(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/auth/login";

        List<Group> groups = groupService.getGroupsForUser(userId);
        model.addAttribute("groups", groups);
        return "fragments/group-list :: groupListContent";
    }

    @GetMapping("/{groupId}/view")
    public String viewGroup(@PathVariable Long groupId, HttpSession session, Model model) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return "redirect:/auth/login";

        model.addAllAttributes(groupViewService.build(groupId, requesterId));
        return "fragments/group-detail :: sceneContent";
    }
    @PostMapping("/{groupId}/members")
    public String addMember(@PathVariable Long groupId,
                            @RequestParam Long userId,
                            HttpSession session,
                            Model model) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return "redirect:/auth/login";

        groupService.addMember(groupId, userId, requesterId);
        model.addAllAttributes(groupViewService.build(groupId, requesterId));
        return "fragments/group-detail :: sceneContent";
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                              HttpSession session) {
        Long requesterId = (Long) session.getAttribute("userId");
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        groupService.deleteGroup(groupId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
