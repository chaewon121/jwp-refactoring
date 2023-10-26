package kitchenpos.table.application;

import kitchenpos.order.application.OrderVerificationEvent;
import kitchenpos.order.repository.OrderTableRepository;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.presentation.dto.TableGroupCreateRequest;
import kitchenpos.table.repository.TableGroupRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class TableGroupService {

    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final ApplicationEventPublisher publisher;

    public TableGroupService(final OrderTableRepository orderTableRepository,
                             final TableGroupRepository tableGroupRepository,
                             final ApplicationEventPublisher publisher) {
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.publisher = publisher;
    }

    public TableGroup create(final TableGroupCreateRequest request) {
        final List<Long> orderTableIds = request.getOrderTables();
        final List<OrderTable> savedOrderTables = orderTableRepository.findAllByIdIn(orderTableIds);
        if (orderTableIds.size() != savedOrderTables.size()) {
            throw new IllegalArgumentException();
        }

        if (request.getOrderTables().size() < 2) {
            throw new IllegalArgumentException("두개 이상의 테이블만 그룹화가 가능합니다.");
        }

        final TableGroup savedTableGroup = tableGroupRepository.save(new TableGroup());

        for (final OrderTable savedOrderTable : savedOrderTables) {
            publisher.publishEvent(new OrderVerificationEvent(this, savedOrderTable.getId()));
            savedOrderTable.group(savedTableGroup);
        }

        return savedTableGroup;
    }

    public void ungroup(final Long tableGroupId) {
        final List<OrderTable> orderTables = orderTableRepository.findAllByTableGroupId(tableGroupId);
        for (final OrderTable orderTable : orderTables) {
            publisher.publishEvent(new OrderVerificationEvent(this, orderTable.getId()));
            orderTable.unGroup();
        }
    }
}
