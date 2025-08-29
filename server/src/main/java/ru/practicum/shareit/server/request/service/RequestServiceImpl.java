package ru.practicum.shareit.server.request.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.dal.ItemRepository;
import ru.practicum.shareit.server.item.dto.ItemInRequestResponseDto;
import ru.practicum.shareit.server.item.dto.ItemMapper;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.request.dto.RequestMapper;
import ru.practicum.shareit.server.request.dto.RequestRequestDto;
import ru.practicum.shareit.server.request.dto.RequestResponseDto;
import ru.practicum.shareit.server.request.dto.RequestWithItemsResponseDto;
import ru.practicum.shareit.server.request.model.Request;
import ru.practicum.shareit.server.request.repository.RequestRepository;
import ru.practicum.shareit.server.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public RequestResponseDto createRequest(RequestRequestDto newRequestDto, Long creatorId) {

        checkUser(creatorId);

        Request newRequest = RequestMapper.toRequest(newRequestDto, creatorId);

        return RequestMapper.toRequestResponseDto(requestRepository.save(newRequest));
    }


    @Override
    public Collection<RequestWithItemsResponseDto> getUserRequests(Long userId) {

        checkUser(userId);

        Collection<Request> requests = requestRepository.findAllByRequestorOrderByCreated(userId);

        return getRequestWithItemsResponseDtos(requests);
    }


    @Override
    public Collection<RequestWithItemsResponseDto> getOtherUsersRequests(Long userId) {

        checkUser(userId);

        Collection<Request> requests = requestRepository.findAllByRequestorIsNot(userId);

        return getRequestWithItemsResponseDtos(requests);
    }

    @Override
    public RequestWithItemsResponseDto getRequestById(Long requestId) {
        Request request = requestRepository.getRequestById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " not found"));

        return RequestMapper.toRequestWithItemsResponseDto(request,
                itemRepository.findAllByRequestIn(Set.of(requestId)).stream()
                        .map(ItemMapper::toItemInRequestResponseDto)
                        .collect(Collectors.toSet()));
    }

    private void checkUser(Long userId) {

        if (userRepository.getUserById(userId).isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
    }

    private Collection<RequestWithItemsResponseDto> getRequestWithItemsResponseDtos(Collection<Request> requests) {

        Collection<Long> requestsIds = requests.stream()
                .map(Request::getId)
                .collect(Collectors.toSet());

        Collection<Item> items = itemRepository.findAllByRequestIn(requestsIds);

        Map<Long, List<Item>> requestsItems = items.stream()
                .collect(Collectors.groupingBy(Item::getRequest));

        return requests.stream()
                .map(request -> {

                    Collection<Item> requestItems = requestsItems.get(request.getId());

                    Collection<ItemInRequestResponseDto> itemsDto = new HashSet<>();

                    if (requestItems != null) {
                        itemsDto =
                                requestItems.stream()
                                        .map(ItemMapper::toItemInRequestResponseDto)
                                        .collect(Collectors.toSet());
                    }

                    return RequestMapper.toRequestWithItemsResponseDto(request, itemsDto);

                })
                .collect(Collectors.toSet());
    }
}