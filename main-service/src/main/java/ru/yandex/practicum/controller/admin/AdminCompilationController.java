package ru.yandex.practicum.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.compilation.CompilationDto;
import ru.yandex.practicum.dto.compilation.NewCompilationDto;
import ru.yandex.practicum.dto.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.service.compilation.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto add(@RequestBody @Valid NewCompilationDto dto) {
        return compilationService.addCompilation(dto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable Long compId,
                                 @RequestBody @Valid UpdateCompilationRequest request) {
        return compilationService.updateCompilation(compId, request);
    }
}
