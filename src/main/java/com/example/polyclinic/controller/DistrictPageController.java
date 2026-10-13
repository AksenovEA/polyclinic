
package com.example.polyclinic.controller;

import com.example.polyclinic.entity.District;
import com.example.polyclinic.repository.DistrictRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/districts")
public class DistrictPageController {

    private final DistrictRepository districtRepository;

    public DistrictPageController(
            DistrictRepository districtRepository
    ) {
        this.districtRepository = districtRepository;
    }

    @GetMapping
    public String districts(Model model) {
        model.addAttribute(
                "districts",
                districtRepository.findAll()
        );

        return "districts";
    }

    @GetMapping("/new")
    public String newDistrictForm(Model model) {
        model.addAttribute("district", new District());
        return "district-form";
    }

    @PostMapping
    public String createDistrict(
            Integer number,
            String address,
            RedirectAttributes redirectAttributes
    ) {
        if (number == null || number <= 0
                || address == null || address.isBlank()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Укажите положительный номер участка и адрес"
            );

            return "redirect:/districts/new";
        }

        District district = new District();
        district.setNumber(number);
        district.setAddress(address.trim());

        try {
            districtRepository.saveAndFlush(district);
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Участок с таким номером уже существует"
            );

            return "redirect:/districts/new";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Участок успешно добавлен"
        );

        return "redirect:/districts";
    }
}
